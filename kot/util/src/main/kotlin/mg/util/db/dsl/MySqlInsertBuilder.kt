package mg.util.db.dsl

import mg.util.common.Common
import mg.util.db.AliasBuilder
import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.functional.Opt2
import mg.util.functional.toOpt
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class MySqlInsertBuilder {

    fun build(@Suppress("UNUSED_PARAMETER") p: Sql.Parameters, sql: Sql): String {

        val dp = DslParameters().apply {
            typeT = sql.t
            uniqueId = UidBuilder.buildUniqueId(sql.t)
            uniqueIdAlias = AliasBuilder.build(uniqueId!!)
        }

        val sqls = mutableListOf<String>()

        sqls += buildInsertSql(sql.t)

        dp.typeT.toOpt()
                .map(FieldCache::fieldsFor)
                .x {
                    if (customs.isNotEmpty() || listsOfCustoms.isNotEmpty()) {
                        sqls += "SELECT LAST_INSERT_ID() INTO @parentLastId"
                    }

                    sqls += customs
                            .map { field -> FieldAccessor.fieldGet(field, dp.typeT) }
                            .map { buildInsertSqlOneToOne(it, sql.t) }

                    sqls += listsOfCustoms
                            .map { field -> FieldAccessor.fieldGet(field, dp.typeT) as List<*> }
                            .map { buildInsertSqlOneToMany(it, sql.t) }
                }

        return sqls.joinToString(";").also { println(it) }
    }

    private fun buildInsertSql(type: Any): String =
            buildInsertSql(type) { uid, fields, fieldsValues ->
                "INSERT INTO $uid ($fields) VALUES ($fieldsValues)"
            }

    private fun buildInsertSqlOneToOne(child: Any, parent: Any): String =
            buildInsertSql(child) { childUid, childFields, childFieldsValues ->
                buildInsertForParentToChildRelation(parent, childUid, childFields, childFieldsValues)
            }

    // TODO: 90 add test coverage: one-to-many relation
    private fun buildInsertSqlOneToMany(children: List<*>, parent: Any): String {
        return children
                .filterNotNull()
                .joinToString(";") {
                    buildInsertSql(it) { childUid, childFields, childFieldsValues ->
                        buildInsertForParentToChildRelation(parent, childUid, childFields, childFieldsValues)
                    }
                }
    }

    private fun buildInsertForParentToChildRelation(parent: Any, childUid: String, childFields: Opt2<String>, childFieldsValues: Opt2<String>): String {
        val parentUid = UidBuilder.buildUniqueId(parent)
        val tableJoinUid = parentUid + childUid

        return "INSERT INTO $childUid ($childFields) VALUES ($childFieldsValues);" +
                "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                "INSERT INTO $tableJoinUid (${parentUid}refid, ${childUid}refid) VALUES (@parentLastId, @childLastId)"
    }

    private fun buildInsertSql(type: Any, insertCreateFunction: (String, Opt2<String>, Opt2<String>) -> String): String {

        if ("array" in (type::class.simpleName ?: "")) {
            return ""
        }

        val memberProperties = type
                .toOpt()
                .map { it::class.memberProperties }
                .lfilter { p: KProperty1<*, *> ->

                    val javaFieldTypeName = p.javaField?.type?.toString() ?: ""

                    p.javaField != null
                            && !Common.isCustom(p.javaField!!)
                            && "Array" !in javaFieldTypeName
                            && "List" !in javaFieldTypeName
                            && "collection" !in javaFieldTypeName
                }

        val fieldNames = memberProperties.map { it.joinToString(", ") { p -> p.name } }
        val fieldValues = memberProperties.map { list: List<KProperty1<*, *>> ->

            list.joinToString(", ") {
                "'${MySqlImpl.getFieldValueAsString(it, type)}'"
            }
        }

        val typeUid = UidBuilder.buildUniqueId(type)

        return insertCreateFunction(typeUid, fieldNames, fieldValues)
    }

}