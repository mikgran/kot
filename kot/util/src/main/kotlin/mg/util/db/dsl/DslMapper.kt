package mg.util.db.dsl

import mg.util.common.Common.isCustom
import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import java.lang.reflect.Field
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

        (p.joins.iterator() +
                p.wheres.iterator() +
                p.updates.iterator())
                .forEach { build(p, it) }

        return build(p, p.action)
    }

    private fun build(p: Parameters, sql: Sql?): String {
        return when (sql) {
            is Sql.Create -> buildCreate(p, sql)
            is Sql.Drop -> buildDrop(p, sql)
            is Sql.Select -> buildSelect(p, sql)
            is Sql.Insert -> buildInsert(p, sql)
            is Sql.Update -> buildUpdate(p, sql)
            is Sql.Delete -> TODO()
            is Sql.Select.Join ->
                buildAndAddJoinFieldAndTableFragments(p, sql)
            is Sql.Select.Where,
            is Sql.Select.Join.Where,
            is Sql.Select.Join.Where.Eq.Where ->
                buildWhereFieldFragment(sql).also { p.whereFragments += it }
            is Sql.Select.Where.Eq,
            is Sql.Select.Join.Where.Eq,
            is Sql.Select.Join.Where.Eq.Where.Eq ->
                buildEqFragment(sql).also { p.whereFragments += it }
            is Sql.Update.Set,
            is Sql.Update.Set.Eq.And ->
                buildUpdateFieldFragments(p, sql).also { p.updateFragments += it }
            is Sql.Update.Set.Eq.Where,
            is Sql.Update.Set.Eq.And.Eq.Where ->
                buildUpdateFieldFragments(p, sql).also { p.whereFragments += it }
            is Sql.Update.Set.Eq,
            is Sql.Update.Set.Eq.And.Eq ->
                buildEqFragment(sql).also { p.updateFragments += it }
            is Sql.Update.Set.Eq.Where.Eq,
            is Sql.Update.Set.Eq.And.Eq.Where.Eq ->
                buildEqFragment(sql).also { p.whereFragments += it }
            null -> throw Exception("Action not supported: null")
            is Sql.Select.Join.On -> TODO()
            is Sql.Select.Join.On.Eq -> TODO()
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
    private fun <T : Any> getFieldValue(field: Field, type: T) = field.get(type)

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

    private fun buildAndAddJoinFieldAndTableFragments(p: Parameters, sql: Sql.Select.Join): String {
        p.fieldFragments.add(buildFieldFragment(sql))
        p.joinFragments.add(buildTableFragment(sql))  // TODO 98 remove
        p.joinTypes.add(sql.t) // TODO 99 remove
        return ""
    }

    private fun buildCreate(p: Parameters, sql: Sql): String {
        return MySqlCreateBuilder().buildCreate(p, sql)
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

    private fun buildSelect(p: Parameters, select: Sql.Select?): String {
        p.tableFragments.add(0, buildTableFragment(select as Sql))
        p.fieldFragments.add(0, buildFieldFragment(select as Sql))
        val joinFragments = buildJoinOnFragment(p, select)

        val whereStr = " WHERE "
        val whereFragmentsSize = p.whereFragments.size
        val whereElementCount = 2 // TOIMPROVE: add(Where(t)) add(Eq(t)) -> count == 2, distinctBy(t::class)?
        return of(StringBuilder())
                .rcv {
                    append("SELECT ${p.fieldFragments.joinToString(", ")}")
                    append(" FROM ${p.tableFragments.joinToString(", ")}")
                }
                .case({ whereFragmentsSize == whereElementCount }, { it.append(whereStr + p.whereFragments.joinToString("")); it })
                .case({ whereFragmentsSize / whereElementCount > 1 }, { it.append(whereStr + (p.whereFragments.chunked(2).joinToString(" AND ") { (i, j) -> "$i$j" })); it })
                .caseDefault { it }
                .result()
                .ifPresent {
                    if (p.joins.isNotEmpty()) {
                        it.append(joinFragments)
                    }
                }
                .get()
                .toString()
    }

    // TODO 110: Replace windowed list handling with hashMap of links handling
    private fun buildJoinOnFragment(p: Parameters, select: Sql.Select): String {

        // construct tree of relations
        // return buildJoinsWithDefaultRef(p, root)

        // X XX: 111 finish this!
        // p.joinsMap[select.t]

        of(linksForParent(select.t))
                .ifPresent { p.joinsMap[select.t] = it }




        return when {
            p.joins.isNotEmpty() -> buildJoinsManually(p, select, isFieldRefPresent(p))
            else -> buildJoinsWithDefaultRef(p, select)
        }
    }

    private fun linksForParent(type: Any): Opt2<List<Any>> {
        return of(type::class.java.declaredFields)
                .lfilter(::isCustom)
                .lxmap<Field, Any> { map { getFieldValue(it, type) } }
    }

    private fun isFieldRefPresent(p: Parameters): Boolean = p.joins.any { it.t is KProperty1<*, *> }

    private fun buildJoinsManually(p: Parameters, root: Sql.Select, isFieldRefPresent: Boolean): String {

        if (isFieldRefPresent) {

        } else {

        }

        return ""
    }

    private fun buildJoinsWithDefaultRef(p: Parameters, root: Sql.Select): String {
        // TODO 98 remodel?
        p.joinTypes.add(0, root.t)
        return of(p.joinTypes)
                .filter { it.size >= 2 && it.size % 2 == 0 }
                .xmap {
                    windowed(size = 2, step = 1, partialWindows = false) { (i, j) ->
                        val (uidI, aliasI) = buildUidAndAlias(i)
                        val (uidJ, aliasJ) = buildUidAndAlias(j)
                        "$uidJ $aliasJ ON ${aliasI}.id = ${aliasJ}.${uidI}refid"
                    }.joinToString("")
                }
                .map { " JOIN $it" }
                .getOrElse("")
    }

    private fun buildTableFragment(sql: Sql): String {
        val (uid, alias) = buildUidAndAlias(sql)
        return "$uid $alias"
    }

    private fun buildFieldFragment(sql: Sql): String {
        val (_, alias) = buildUidAndAlias(sql)
        return sql.t::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
    }

    private fun buildUidAndAlias(sql: Sql): Pair<String, String> = buildUidAndAlias(sql.t)
    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val alias = AliasBuilder.build(uid)
        return uid to alias
    }

}

class MySqlDslMapper : DslMapper()
class OracleDslMapper : DslMapper()