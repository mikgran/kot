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

    fun build(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {

        // FIXME: 10000 Needs parent-child relationship HashMap
        // k: OBMultipleComposition v: OBSimple, OBSimpleComp
        // k: OBSimpleComp v: OBSubComp
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
        val dp = buildDslParameters(sql.t)
        val sqls = mutableListOf<String>()

        sqls += buildSqlCreate(dp)

        dp.typeT.toOpt()
                .map(FieldCache::fieldsFor)
                .x {
                    sqls += customs.map { buildSqlCreateForChild(dp, fieldGet(it, dp.typeT)) }
                    sqls += listsOfCustoms.map { buildSqlCreateForChild(dp, (fieldGet(it, dp.typeT) as List<*>)[0]) }
                }

        return sqls.joinToString(";")
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
