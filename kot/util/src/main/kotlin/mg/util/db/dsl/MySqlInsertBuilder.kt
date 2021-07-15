package mg.util.db.dsl

import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.functional.Opt2
import mg.util.functional.mapIf
import mg.util.functional.toOpt

class MySqlInsertBuilder {

    val idBuilder = IncrementalIdBuilder()

    fun build(@Suppress("UNUSED_PARAMETER") p: Sql.Parameters, sql: Sql): String {

        val sqls = mutableListOf<String>()
        val type = sql.t

        FieldAccessor.uniquesByParent(type).toOpt()
                .x {
                    putIfAbsent(type, listOf())
                    entries.forEachIndexed { index, entry -> buildInsertIntos(index, entry, sqls) }
                }

        return sqls.joinToString(";").also { println(it) }
    }

    private fun buildInsertIntos(index: Int, entry: MutableMap.MutableEntry<Any, List<Any>>, sqls: MutableList<String>) {
        val parent = entry.key
        val parentUid = UidBuilder.buildUniqueId(parent)
        val parentLastId =
                (index == 0).mapIf { idBuilder.next(parentUid) }
                        .ifEmpty { idBuilder[parentUid] ?: 0 }
                        .map { parentUid + it }

        parent.toOpt()
                .map(FieldCache::fieldsFor)
                .x {
                    sqls += buildInsert(parent)

                    (customs.isNotEmpty() || listsOfCustoms.isNotEmpty())
                            .mapIf { sqls += "SELECT LAST_INSERT_ID() INTO @$parentLastId" }

                    sqls += customs
                            .map { field -> FieldAccessor.fieldGet(field, parent) }
                            .map { buildOneToOne(it, parent) }

                    sqls += listsOfCustoms
                            .map { field -> FieldAccessor.fieldGet(field, parent) as List<*> }
                            .map { buildOneToMany(it, parent) }
                }
    }

    private fun buildInsert(type: Any): String =
            buildInsert(type) { uid, fields, fieldsValues ->
                "INSERT INTO $uid ($fields) VALUES ($fieldsValues)"
            }

    private fun buildOneToOne(child: Any, parent: Any): String =
            buildInsert(child) { childUid, childFields, childFieldsValues ->
                buildParentToChild(parent, childUid, childFields, childFieldsValues)
            }

    // TODO: 90 add test coverage: one-to-many relation
    private fun buildOneToMany(children: List<*>, parent: Any): String {
        return children
                .filterNotNull()
                .joinToString(";") {
                    buildInsert(it) { childUid, childFields, childFieldsValues ->
                        buildParentToChild(parent, childUid, childFields, childFieldsValues)
                    }
                }
    }

    private fun buildParentToChild(parent: Any, childUid: String, childFields: Opt2<String>, childFieldsValues: Opt2<String>): String {
        val parentUid = UidBuilder.buildUniqueId(parent)
        val tableJoinUid = parentUid + childUid
        val parentLastId = parentUid + idBuilder[parentUid]
        val childLastId = childUid + idBuilder.next(childUid)

        return "INSERT INTO $childUid ($childFields) VALUES ($childFieldsValues);" +
                "SELECT LAST_INSERT_ID() INTO @$childLastId;" +
                "INSERT INTO $tableJoinUid (${parentUid}refid, ${childUid}refid) VALUES (@$parentLastId, @$childLastId)"
    }

    private fun buildInsert(type: Any, insertCreateFunction: (String, Opt2<String>, Opt2<String>) -> String): String {

        val fields = FieldCache.fieldsFor(type)
        val fieldNames = fields.primitives.joinToString(", ") { p -> p.name }
        val fieldValues = fields.primitives.joinToString(", ") { "'${FieldAccessor.fieldGet(it, type)}'" }
        val typeUid = UidBuilder.buildUniqueId(type)

        return insertCreateFunction(typeUid, fieldNames.toOpt(), fieldValues.toOpt())
    }

}

