package mg.util.db.dsl

import mg.util.common.stackIterator
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
open class DslMapper {

    // OracleMapper() : DslMapper
    // MySqlMapper() : DslMapper
    // DB2Mapper() : DslMapper
    // TODO: 12 add all MySql mapping functions

    fun map(dsl: Sql): String {
        return of(dsl)
                .map(::build)
                .getOrElseThrow { Exception("map: Unable to build sql for dsl: $dsl") }!!
    }

    private fun build(sql: Sql): String {

        val p = sql.parameters()

        p.joins.forEach { build(p, it) }
        p.wheres.forEach { build(p, it) }
        p.updates.forEach { build(p, it) }

//        val iterator = p.joins.stackIterator() + p.wheres + p.joins
//        iterator.forEach { build(p, it) }

        return build(p, p.action)
    }

    private fun build(p: Parameters, sql: Sql?): String {
        return when (sql) {
            is Sql.Select -> buildSelect(p, sql)
            is Sql.Select.Join -> buildAndAddJoinFieldAndTableFragments(p, sql)
            is Sql.Select.Where,
            is Sql.Select.Join.Where,
            is Sql.Select.Join.Where.Eq.Where,
            is Sql.Select.Join.Where.Eq.Where.Eq -> buildWhereFieldFragment(sql).also { p.whereFragments += it }
            is Sql.Select.Where.Eq,
            is Sql.Select.Join.Where.Eq -> buildEqFragment(sql).also { p.whereFragments += it }
            is Sql.Delete -> ""
            is Sql.Create -> buildCreate(p, sql)
            is Sql.Insert -> buildInsert(p, sql)
            is Sql.Drop -> buildDrop(p, sql)
            is Sql.Update -> buildUpdate(p, sql)
            is Sql.Update.Set,
            is Sql.Update.Set.Eq.And -> buildUpdateFieldFragments(p, sql).also { p.updateFragments += it }
            is Sql.Update.Set.Eq.Where,
            is Sql.Update.Set.Eq.And.Eq.Where -> buildUpdateFieldFragments(p, sql).also { p.whereFragments += it }
            is Sql.Update.Set.Eq,
            is Sql.Update.Set.Eq.And.Eq -> buildEqFragment(sql).also { p.updateFragments += it }
            is Sql.Update.Set.Eq.Where.Eq,
            is Sql.Update.Set.Eq.And.Eq.Where.Eq -> buildEqFragment(sql).also { p.whereFragments += it }
            null -> throw Exception("Action not supported: null")
        }
    }

    private fun buildInsert(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {

        val padding1 = "INSERT INTO ${UidBuilder.buildUniqueId(sql.t)} ("
        val padding2 = ") VALUES ("
        val padding3 = ")"

        val properties = of(sql.t)
                .map { it::class.memberProperties.toCollection(ArrayList()) }

        val fieldsCommaSeparated = of(properties)
                .map { it.joinToString(", ") { p -> p.name } }

        val fieldsValuesCommaSeparated = of(properties)
                .map { it.joinToString(", ") { p -> "'${getFieldValueAsString(p, sql.t)}'" } }

        return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"
    }

    private fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()

    private fun buildDrop(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {
        return "DROP TABLE IF EXISTS ${UidBuilder.buildUniqueId(sql.t)}"
    }

    private fun buildUpdateFieldFragments(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String =
            of(sql.t).mapTo(KProperty1::class)
                    .map { it.name }
                    .get()
                    .toString()

    private fun buildUpdate(p: Parameters, sql: Sql): String {

        // "UPDATE $uid SET firstName = 'newFirstName', lastName = 'newLastName' WHERE firstName = 'firstName'"
        val builder = of(StringBuilder())
                .rcv {
                    append("UPDATE ")
                    append(UidBuilder.buildUniqueId(sql.t))
                    append(" SET ")
                    append(p.updateFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    if (p.whereFragments.isNotEmpty()) {
                        append(" WHERE ")
                        append(p.whereFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    }
                }

        return builder.get().toString()
    }

    private fun buildAndAddJoinFieldAndTableFragments(info: Parameters, sql: Sql.Select.Join): String {
        info.fieldFragments += buildFieldFragment(sql)
        info.joinFragments += buildTableFragment(sql)
        return ""
    }

    private fun buildCreate(info: Parameters, sql: Sql): String {
        return MySqlCreateBuilder().buildCreate(info, sql)
    }

    private fun buildEqFragment(sql: Sql): String = of(sql.t).map { " = '$it'" }.toString()

    private fun buildWhereFieldFragment(sql: Sql): String {

        val kProperty1 = of(sql.t).mapTo(KProperty1::class)

        val alias = kProperty1
                .map { it.javaField?.declaringClass?.kotlin }
                .map(UidBuilder::build)
                .map(AliasBuilder::build)

        return kProperty1
                .mapWith(alias) { p, a -> "${a}.${p.name}" }
                .toString()
    }

    private fun buildSelect(param: Parameters, select: Sql.Select?): String {
        param.tableFragments.add(0, buildTableFragment(select as Sql))
        param.fieldFragments.add(0, buildFieldFragment(select as Sql))

        val whereStr = " WHERE "
        val whereFragmentsSize = param.whereFragments.size
        val whereElementCount = 2 // TOIMPROVE: add(Where(t)) add(Eq(t)) -> count == 2, distinctBy(t::class)?
        return of(StringBuilder())
                .rcv {
                    append("SELECT ${param.fieldFragments.joinToString(", ")}")
                    append(" FROM ${param.tableFragments.joinToString(", ")}")
                }
                .case({ whereFragmentsSize == whereElementCount }, { it.append(whereStr + param.whereFragments.joinToString("")); it })
                .case({ whereFragmentsSize / whereElementCount > 1 }, { it.append(whereStr + (param.whereFragments.chunked(2).joinToString(" AND ") { (i, j) -> "$i$j" })); it })
                .caseDefault { it }
                .result()
                .ifPresent {
                    if (param.joins.isNotEmpty()) {
                        it.append(" JOIN ")
                        it.append(param.joinFragments.joinToString(", "))
                    }
                }
                .get()
                .toString()
    }

    private fun buildTableFragment(sql: Sql): String {
        val (uid, alias) = buildUidAndAlias(sql)
        return "$uid $alias"
    }

    private fun buildFieldFragment(sql: Sql): String {
        val (_, alias) = buildUidAndAlias(sql)
        return sql.t::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
    }

    private fun buildUidAndAlias(sql: Sql?): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(sql?.t ?: "")
        val alias = AliasBuilder.build(uid)
        return uid to alias
    }

}

class MySqlDslMapper : DslMapper()
class OracleDslMapper : DslMapper()