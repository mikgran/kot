package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.db.dsl.FieldAccessor.Companion.fieldGet
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2
import mg.util.functional.toOpt
import kotlin.reflect.full.declaredMemberProperties

open class MySqlCreateBuilder {

    /*
        Parent(
                aValue = 1,
                oneToOneChild = Child(aStr = "AAAA"),
                oneToManyChildren = listOf(
                        ListChild(
                                aStr = "AAAA",
                                subChild = SubChild(11)),
                        ListChild(
                                aStr = "BBBB",
                                subChild = SubChild(22)))
        )
    */
    fun build(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {

        return FieldAccessor.uniquesByParent(sql.t).toOpt()
                .filter { it.isNotEmpty() }
                .getOrElse { hashMapOf(sql.t to emptyList()) }
                .map(::buildParentAndChildSqls)
                .flatten()
                .flatMap { it.split(";") }
                .distinctBy { it }
                .joinToString(";")
    }

    private fun buildParentAndChildSqls(entry: Map.Entry<Any, List<Any>>): List<String> {
        val dslParameters = buildDslParameters(entry.key)

        val sqls = mutableListOf<String>()
        sqls += buildSqlCreate(dslParameters)

        FieldCache.fieldsFor(entry.key).toOpt()
                .map { fields ->
                    fields.customs.map { fieldGet(it, entry.key) } +
                            fields.listsOfCustoms.mapNotNull { (fieldGet(it, entry.key) as List<*>)[0] }
                }
                .x { sqls += map { buildSqlCreateForChild(dslParameters, it) } }

        return sqls
    }

    private fun buildSqlCreateForChild(parentDp: DslParameters, t: Any?): String {

        val childDp = Opt2.of(t)
                .map(::buildDslParameters)
                .getOrElseThrow { Exception("Unable to getField($t) and to build DslParameters.") }!!

        val childSqlCreate = buildSqlCreate(childDp)
        val childAlterTable = buildCreateTableForJoinTable(parentDp, childDp)

        return "$childSqlCreate;$childAlterTable"
    }

    private fun buildCreateTableForJoinTable(parentDp: DslParameters, childDp: DslParameters): String {
        return "CREATE TABLE IF NOT EXISTS ${parentDp.uniqueId}${childDp.uniqueId}" +
                "(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "${parentDp.uniqueId}refid MEDIUMINT NOT NULL, " +
                "${childDp.uniqueId}refid MEDIUMINT NOT NULL)"
    }

    private fun buildDslParameters(t: Any): DslParameters {
        return DslParameters().apply {
            typeT = t
            uniqueId = UidBuilder.buildUniqueId(t)
            uniqueIdAlias = AliasBuilder.build(uniqueId!!)
        }
    }

    private fun buildSqlCreate(dp: DslParameters): String {
        val mapper = MySqlTypeMapper()
        val fieldsSql = Opt2.of(dp.typeT)
                .map {
                    it::class.declaredMemberProperties
                            .map(mapper::getTypeString)
                            .filter(String::isNotEmpty)
                }
                .filter(List<*>::isNotEmpty)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        return "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, $fieldsSql)"
    }
}
