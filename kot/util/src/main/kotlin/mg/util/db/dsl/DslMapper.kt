package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.common.Common.isCustom
import mg.util.common.Common.isList
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2.Factory.of
import java.lang.reflect.Field
import kotlin.collections.MutableMap.MutableEntry
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
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
            is Sql.Select.Join -> buildJoinFragmentAndJoinColumnFragments(p, sql)
            is Sql.Select.Join.On -> buildJoinOnFragment(p, sql)
            is Sql.Select.Join.On.Eq -> buildJoinOnEqFragment(p, sql)
            is Sql.Select.Join.Where -> buildWhereFragment(p, sql)
            is Sql.Select.Join.Where.Eq -> buildWhereEqFragment(p, sql)
            is Sql.Select.Join.Where.Eq.Where -> buildWhereFragment(p, sql)
            is Sql.Select.Join.Where.Eq.Where.Eq -> buildWhereEqFragment(p, sql)
            is Sql.Select.Where -> buildWhereFragment(p, sql)
            is Sql.Select.Where.Eq -> buildWhereEqFragment(p, sql)
            is Sql.Update.Set -> buildUpdateFragments(p, sql)
            is Sql.Update.Set.Eq -> buildUpdateEqFragment(p, sql)
            is Sql.Update.Set.Eq.And -> buildUpdateFragments(p, sql)
            is Sql.Update.Set.Eq.And.Eq -> buildUpdateEqFragment(p, sql)
            is Sql.Update.Set.Eq.And.Eq.Where -> buildWhereFragment(p, sql)
            is Sql.Update.Set.Eq.And.Eq.Where.Eq -> buildWhereEqFragment(p, sql)
            is Sql.Update.Set.Eq.Where -> buildWhereFragment(p, sql)
            is Sql.Update.Set.Eq.Where.Eq -> buildWhereEqFragment(p, sql)
            null -> throw Exception("Action not supported: null")
        }
    }

    private fun buildJoinOnEqFragment(p: Parameters, sql: Sql.Select.Join.On.Eq): String {
        val ref = sql.t as KProperty1<*, *>
        val uid = UidBuilder.build(ref.javaField?.declaringClass?.kotlin ?: Any::class)
        val alias = AliasBuilder.build(uid)
        p.joinFragments += "= $alias.${ref.name}"
        return ""
    }

    private fun buildJoinOnFragment(p: Parameters, sql: Sql.Select.Join.On): String {
        val uid = UidBuilder.build(sql.t as KClass<*>)
        val alias = AliasBuilder.build(uid)
        p.joinFragments += "ON ${alias}.id"
        return ""
    }

    private fun buildJoinFragmentAndJoinColumnFragments(p: Parameters, sql: Sql.Select.Join): String {
        p.columnFragments += buildFieldFragment(sql.t)
        p.joinFragments += "JOIN ${buildTableFragment(sql.t)}"
        return ""
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
    private fun <T : Any> getFieldValue(field: Field, type: T): Any? {
        field.isAccessible = true
        return field.get(type)
    }

    private fun buildDrop(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {
        return "DROP TABLE IF EXISTS ${UidBuilder.buildUniqueId(sql.t)}"
    }

    private fun buildUpdateFragments(p: Parameters, sql: Sql): String {
        p.updateFragments += of(sql.t)
                .mapTo(KProperty1::class)
                .map { it.name }
                .get()
                .toString()
        return ""
    }

    private fun buildUpdate(p: Parameters, sql: Sql): String {
        // "UPDATE $uid $alias SET firstName = 'newFirstName', lastName = 'newLastName' WHERE $alias.firstName = 'firstName'"
        val uid = UidBuilder.buildUniqueId(sql.t)
        val alias = AliasBuilder.build(uid)
        val builder = StringBuilder()
                .apply {
                    append("UPDATE ")
                    append(uid)
                    append(" ")
                    append(alias)
                    append(" SET ")
                    append(p.updateFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    if (p.whereFragments.isNotEmpty()) {
                        append(" WHERE ")
                        append(p.whereFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    }
                }

        return builder.toString()
    }

    private fun buildCreate(p: Parameters, sql: Sql): String {
        return MySqlCreateBuilder().buildCreate(p, sql) // TODO: -150 does not include multilayer creates yet, automate for less hassle.
    }

    private fun buildUpdateEqFragment(p: Parameters, sql: Sql): String {
        p.updateFragments += of(sql.t).map { " = '$it'" }.toString()
        return ""
    }

    private fun buildWhereEqFragment(p: Parameters, sql: Sql): String {
        p.whereFragments += of(sql.t).map { " = '$it'" }.toString()
        return ""
    }

    private fun buildWhereFragment(p: Parameters, sql: Sql): String {
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

    private fun buildSelect(p: Parameters, select: Sql.Select): String {
        p.tableFragments.add(0, buildTableFragment(select.t))
        buildRefsTree(select.t, p)
        buildSelectColumns(p)
        buildJoins(p)

        val sb = StringBuilder() +
                "SELECT ${p.columnFragments.joinToString(", ")}" +
                " FROM ${p.tableFragments.joinToString(", ")}" +
                buildWhereFragments(p) +
                buildJoinFragments(p)
        return sb.toString()
    }

    private fun buildJoinFragments(p: Parameters): String =
            if (p.joinFragments.isNotEmpty()) " " + p.joinFragments.joinToString(" ") else ""

    private fun buildWhereFragments(p: Parameters): String {
        val whereStr = " WHERE "
        val whereFragmentsSize = p.whereFragments.size
        val whereElementCount = 2 // TOIMPROVE: add(Where(t)) add(Eq(t)) -> count == 2, distinctBy(t::class)?
        return when {
            whereFragmentsSize == whereElementCount -> whereStr + p.whereFragments.joinToString("")
            whereFragmentsSize / whereElementCount > 1 -> {
                whereStr + p.whereFragments
                        .chunked(2)
                        .joinToString(" AND ") { (i, j) -> "$i$j" }
            }
            else -> ""
        }
    }

    private fun buildSelectColumns(p: Parameters) {
        collectUniquesFromJoinsMapAndAction(p)
                .forEach { p.columnFragments += buildFieldFragment(it) }
    }

    private fun collectUniquesFromJoinsMapAndAction(p: Parameters): MutableSet<Any> {
        val uniques = mutableSetOf<Any>()
        of(p.action?.t)
                .map(uniques::add)
        of(p.joinsMap.iterator())
                .lmap { entry: MutableEntry<Any, Any> ->
                    uniques += entry.key
                    (entry.value as? List<*>)?.filterNotNull()?.forEach { uniques += it }
                    entry
                }
        return uniques
    }

    private fun buildJoins(p: Parameters) {
        of(buildJoinsOnNaturalRefs(p))
                .filter(String::isNotEmpty)
                .map(p.joinFragments::add)
    }

    private fun buildRefsTree(root: Any, p: Parameters) {
        of(root)
                .map {
                    val list = linksForParent(it)
                    if (list.isNotEmpty()) {
                        p.joinsMap[it] = list
                    }
                    list
                }
                .xmap { forEach { buildRefsTree(it, p) } }
    }

    private fun linksForParent(type: Any): List<Any> {
        return of(type::class.java.declaredFields.toList())
                .lfilter(::isCustom or ::isList)
                .lxmap<Field, Any> { mapNotNull { getFieldValue(it, type) } }
                .getOrElse { emptyList() }
    }

    private fun buildJoinsOnNaturalRefs(p: Parameters): String {
        return of(p.joinsMap)
                .xmap { map { buildJoinsOnNaturalRefs(it) }.joinToString(" AND ") }
                .filter(::hasContent)
                .map { "JOIN $it" }
                .getOrElse("")
    }

    private fun buildJoinsOnNaturalRefs(entry: Map.Entry<Any, Any>): String {
        val (uidRoot, aliasRoot) = buildUidAndAlias(entry.key)
        return of(entry.value as? List<*>)
                .lmap { type: Any ->
                    val (uidRef, aliasRef) = buildUidAndAlias(type)
                    "$uidRef $aliasRef ON ${aliasRoot}.id = ${aliasRef}.${uidRoot}RefId"
                }
                .xmap { joinToString(" AND ") }
                .getOrElse("")
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