package mg.util.db.dsl

import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
open class DslMapper {

    // OracleMapper() : DslMapper
    // MySqlMapper() : DslMapper
    // DB2Mapper() : DslMapper
    // TODO: 2 add all MySql mapping functions

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
        return when (sql) { // XXX 10 finish me! move all functionality into their own sub classes under the build() function
            is Sql.Create,
            is Sql.Drop,
            is Sql.Select,
            is Sql.Insert,
            is Sql.Update -> sql.build(p)
            is Sql.Delete -> "" // TODO 1
            is Sql.Select.Join -> buildJoinAndJoinColumnsParts(p, sql)
            is Sql.Select.Join.On -> buildSelectJoinOn(p, sql)
            is Sql.Select.Join.On.Eq -> buildSelectJoinOnEq(p, sql)
            is Sql.Select.Join.Where -> buildWherePart(p, sql)
            is Sql.Select.Join.Where.Eq -> buildWhereEqPart(p, sql)
            is Sql.Select.Join.Where.Eq.Where -> buildWherePart(p, sql)
            is Sql.Select.Join.Where.Eq.Where.Eq -> buildWhereEqPart(p, sql)
            is Sql.Select.Where -> buildWherePart(p, sql)
            is Sql.Select.Where.Eq -> buildWhereEqPart(p, sql)
            is Sql.Update.Set -> buildUpdateSet(p, sql)
            is Sql.Update.Set.Eq -> buildUpdateSetEq(p, sql)
            is Sql.Update.Set.Eq.And -> buildUpdateSet(p, sql)
            is Sql.Update.Set.Eq.And.Eq -> buildUpdateSetEq(p, sql)
            is Sql.Update.Set.Eq.And.Eq.Where -> buildWherePart(p, sql)
            is Sql.Update.Set.Eq.And.Eq.Where.Eq -> buildWhereEqPart(p, sql)
            is Sql.Update.Set.Eq.Where -> buildWherePart(p, sql)
            is Sql.Update.Set.Eq.Where.Eq -> buildWhereEqPart(p, sql)
            null -> throw Exception("Action not supported: null")
        }
    }

    private fun buildSelectJoinOnEq(p: Parameters, sql: Sql.Select.Join.On.Eq): String {
        val ref = sql.t as KProperty1<*, *>
        val uid = UidBuilder.build(ref.javaField?.declaringClass?.kotlin ?: Any::class)
        val alias = AliasBuilder.build(uid)
        p.joinFragments += "= $alias.${ref.name}"
        return ""
    }

    private fun buildSelectJoinOn(p: Parameters, sql: Sql.Select.Join.On): String {
        val uid = UidBuilder.build(sql.t as KClass<*>)
        val alias = AliasBuilder.build(uid)
        p.joinFragments += "ON ${alias}.id"
        return ""
    }

    private fun buildJoinAndJoinColumnsParts(p: Parameters, sql: Sql.Select.Join): String {
        p.columnFragments += buildFieldFragment(sql.t)
        p.joinFragments += "JOIN ${buildTableFragment(sql.t)}"
        return ""
    }

    private fun buildUpdateSet(p: Parameters, sql: Sql): String {
        p.updateFragments += of(sql.t)
                .mapTo(KProperty1::class)
                .map { it.name }
                .get()
                .toString()
        return ""
    }

    private fun buildUpdateSetEq(p: Parameters, sql: Sql): String {
        p.updateFragments += of(sql.t).map { " = '$it'" }.toString()
        return ""
    }

    private fun buildWhereEqPart(p: Parameters, sql: Sql): String {
        p.whereFragments += of(sql.t).map { " = '$it'" }.toString()
        return ""
    }

    private fun buildWherePart(p: Parameters, sql: Sql): String {
        val kProperty1 = of(sql.t).mapTo(KProperty1::class)

        val alias = kProperty1
                .map { it.javaField?.declaringClass?.kotlin }
                .map(UidBuilder::build)
                .map(AliasBuilder::build)

        p.whereFragments += kProperty1
                .mapWith(alias) { property, alias1 -> "${alias1}.${property.name}" }
                .toString()

        return ""
    }

    private fun buildTableFragment(type: Any): String {
        val (uid, alias) = buildUidAndAlias(type)
        return "$uid $alias"
    }

    private fun buildFieldFragment(type: Any): String {
        val (_, alias) = buildUidAndAlias(type)
        return type::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
    }

    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val alias = AliasBuilder.build(uid)
        return uid to alias
    }

}

class MySqlDslMapper : DslMapper()
class OracleDslMapper : DslMapper()