package mg.util.db.dsl

import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.functional.Opt2
import mg.util.functional.mapIf
import mg.util.functional.toOpt

class MySqlInsertBuilder {

    fun build(@Suppress("UNUSED_PARAMETER") p: Sql.Parameters, sql: Sql): String {

        val sqls = mutableListOf<String>()
        val type = sql.t

        FieldAccessor.uniquesByParent(type).toOpt()
                .mapWhen({ it.isEmpty() }) { hashMap -> hashMap[type] = emptyList(); hashMap }
                .x {
                    entries.forEach { buildParentAndItsChildSqls(it, sqls) }
                }

        return sqls.joinToString(";").also { println(it) }
    }

    private fun buildParentAndItsChildSqls(entry: MutableMap.MutableEntry<Any, List<Any>>, sqls: MutableList<String>) {
        val parent = entry.key
        parent.toOpt()
                .map(FieldCache::fieldsFor)
                .x {
                    sqls += buildInsertSql(parent)

                    (customs.isNotEmpty() || listsOfCustoms.isNotEmpty())
                            .mapIf { sqls += "SELECT LAST_INSERT_ID() INTO @parentLastId" }

                    sqls += customs
                            .map { field -> FieldAccessor.fieldGet(field, parent) }
                            .map { buildInsertSqlOneToOne(it, parent) }

                    sqls += listsOfCustoms
                            .map { field -> FieldAccessor.fieldGet(field, parent) as List<*> }
                            .map { buildInsertSqlOneToMany(it, parent) }
                }
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

        val fieldsT = FieldCache.fieldsFor(type)
        val fieldNames = fieldsT.primitives.joinToString(", ") { p -> p.name }
        val fieldValues = fieldsT.primitives.joinToString(", ") { "'${FieldAccessor.fieldGet(it, type)}'" }
        val uidT = UidBuilder.buildUniqueId(type)

        return insertCreateFunction(uidT, fieldNames.toOpt(), fieldValues.toOpt())
    }

}