package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.FieldAccessor.Companion.fieldGet
import mg.util.db.dsl.FieldAccessor.Companion.getFieldsWithCustoms
import mg.util.db.dsl.FieldAccessor.Companion.getFieldsWithListOfCustoms
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2
import kotlin.reflect.full.declaredMemberProperties

open class MySqlCreateBuilder {

    fun buildCreate(@Suppress("UNUSED_PARAMETER") p: Parameters, sql: Sql): String {

        val dp = buildDslParameters(sql.t)
        val sqls = mutableListOf<String>()

        sqls += buildSqlCreate(dp)

        // direct one to one Objects:
        sqls += getFieldsWithCustoms(dp) // TOIMPROVE: add more test coverage
                .map { buildSqlCreateForChild(dp, fieldGet(it, dp.typeT)) }

        // lists of Objects:
        sqls += getFieldsWithListOfCustoms(dp)
                .map { buildSqlCreateForOneToMany(dp, (fieldGet(it, dp.typeT) as List<*>)[0]) }

        return sqls.joinToString(";")
    }

    private fun buildSqlCreateForChild(parentDp: DslParameters, t: Any?): String =
            buildSqlCreateForChild(parentDp, t) { childDp ->
                "ALTER TABLE ${parentDp.uniqueId} ADD COLUMN ${childDp.uniqueId}refId MEDIUMINT NOT NULL"
            }

    private fun buildSqlCreateForOneToMany(parentDp: DslParameters, t: Any?): String =
            buildSqlCreateForChild(parentDp, t) { childDp ->
                "CREATE TABLE IF NOT EXISTS ${parentDp.uniqueId}to${childDp.uniqueId}" +
                        "(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "${parentDp.uniqueId}refid MEDIUMINT NOT NULL, " +
                        "${childDp.uniqueId}refid MEDIUMINT NOT NULL)"
            }

    private fun buildSqlCreateForChild(parentDp: DslParameters, t: Any?, relationFunc: (DslParameters) -> String): String {

        val childDp = Opt2.of(t)
                .map(::buildDslParameters)
                .getOrElseThrow { Exception("Unable to get field for $t and to build DslParameters.") }!!

        val childSqlCreate = buildSqlCreate(childDp)

        // ALTER TABLE floors ADD COLUMN buildingId INT NOT NULL;
        val childAlterTable = relationFunc(childDp)

        return "$childSqlCreate;$childAlterTable"
    }

    private fun buildDslParameters(t: Any): DslParameters {
        return DslParameters().apply {
            typeT = t
            uniqueId = UidBuilder.buildUniqueId(t)
            uniqueIdAlias = AliasBuilder.build(uniqueId!!)
        }
    }

    private fun buildSqlCreate(dp: DslParameters): String {
        val fieldsSql = Opt2.of(dp.typeT)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        return "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, $fieldsSql)"
    }

    open fun buildSqlFieldDefinitions(type: Any): List<String> {
        val mapper = MySqlTypeMapper()
        return Opt2.of(type)
                .map { it::class.declaredMemberProperties }
                .xmap { map(mapper::getTypeString).filter(String::isNotEmpty) }
                .getOrElse(emptyList())
    }


}