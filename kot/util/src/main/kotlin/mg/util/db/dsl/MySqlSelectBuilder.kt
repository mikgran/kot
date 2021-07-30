package mg.util.db.dsl

import mg.util.common.Common
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.db.dsl.FieldAccessor.Companion.isCustom
import mg.util.db.dsl.FieldAccessor.Companion.isList
import mg.util.db.dsl.MySqlImpl.Companion.buildUidAndAlias
import mg.util.functional.toOpt
import java.lang.reflect.Field

class MySqlSelectBuilder {

    fun build(p: Sql.Parameters, sql: Sql): String {

        val t = sql.t
        p.tableFragments.add(0, buildTableFragment(t))
        // p.joinsMap.putAll(buildJoinsMap(t, mutableMapOf()))

        FieldAccessor.childrenByParent(t)
                .also {
                    p.joinsMap.putAll(it)
                }

        // XXX: 100000, joinsmap is not getting all sub-relations
        p.joinsMap.also { Common.printClassSimpleNames(it.toMap()) }

        p.toOpt()
                .map { buildJoinsForNaturalRefs(it) }
                .filter(String::isNotEmpty)
                .map(p.joinFragments::add)

        collectUniqueTypesFrom(p.action, p.joinsMap)
                .forEach { addColumnFragments(it, p) }

        val sb = StringBuilder() +
                "SELECT ${p.columnFragments.joinToString(", ")}" +
                " FROM ${p.tableFragments.joinToString(", ")}" +
                buildJoinPart(p) +
                buildManualJoinPart(p) +
                buildWherePart(p)

        return sb.toString()
    }

    private fun addPrimaryIdIfIncluded(p: Sql.Parameters, t: Any) {
        if (p.isPrimaryIdIncluded) {
            val (_, aliasT) = buildUidAndAlias(t)
            p.columnFragments += "$aliasT.id"
        }
    }

    private fun addColumnFragments(type: Any, p: Sql.Parameters) {
        val (_, alias) = buildUidAndAlias(type)

        addPrimaryIdIfIncluded(p, type)

        // TODO 5 check for types coverage
        p.columnFragments += FieldCache.fieldsFor(type)
                .primitives
                .joinToString(", ") { "$alias.${it.name}" }
    }

    private fun buildJoinsForNaturalRefs(parameters: Sql.Parameters): String {
        return parameters
                .joinsMap
                .toOpt()
                .xmap { map { buildJoinsOnNaturalRefs(it) }.joinToString(" ") }
                .filter(Common::hasContent)
                .getOrElse { "" }
    }

    /*
        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
            " FROM $placeUid $p" +
            " JOIN $placeUid$addressUid $joinTableAlias ON $joinTableAlias.${placeUid}refid = $p.id" +
            " JOIN $addressUid $a ON ${joinTableAlias}.${addressUid}refid = $a.id"
         */
    private fun buildJoinsOnNaturalRefs(parentKeyChildValues: Map.Entry<Any, Any>): String {
        val (parentUid, parentAlias) = buildUidAndAlias(parentKeyChildValues.key)
        return (parentKeyChildValues.value as? List<*>).toOpt()
                .lmap { child: Any -> buildNaturalRefForParent(child, parentUid, parentAlias) }
                .xmap { joinToString(" ") }
                .getOrElse { "" }
    }

    private fun buildNaturalRefForParent(t: Any, parentUid: String, parentAlias: String): String {

        return t.toOpt()
                .mapTo(List::class)
                .map(List<*>::first)
                .ifEmpty { t }
                .map { element ->
                    val (childUid, childAlias) = buildUidAndAlias(element)
                    val joinTableAlias = AliasBuilder.build("$parentUid$childUid")
                    "JOIN $parentUid$childUid $joinTableAlias ON $joinTableAlias.${parentUid}refid = $parentAlias.id" +
                            " JOIN $childUid $childAlias ON $joinTableAlias.${childUid}refid = $childAlias.id"
                }
                .getOrElse { "" }
    }

    private fun buildWherePart(p: Sql.Parameters): String {
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

    private fun buildJoinPart(p: Sql.Parameters) =
            (if (p.joinFragments.isNotEmpty()) " ${p.joinFragments.joinToString(" ")}" else "")

    private fun buildManualJoinPart(p: Sql.Parameters): String =
            if (p.manualJoinFragments.isNotEmpty()) " ${p.manualJoinFragments.joinToString(" ")}" else ""

    private fun buildTableFragment(type: Any): String {
        val (uid, alias) = buildUidAndAlias(type)
        return "$uid $alias"
    }

    private fun buildJoinsMap(root: Any, joinsMap: MutableMap<Any, List<Any>>): MutableMap<Any, List<Any>> {
        root.toOpt()
                .map {
                    val children: List<Any> = childrenForParent(it)
                    if (children.isNotEmpty()) {
                        joinsMap[it] = children
                    }
                    children
                }
                .lxforEach<Any> { buildJoinsMap(it, joinsMap) }  // recursion
        return joinsMap
    }

    private fun childrenForParent(type: Any): List<Any> {
        return type::class.java.declaredFields.toList()
                .toOpt()
                .lfilter(::isCustom or ::isList)
                .lxmap<Field, Any> {
                    mapNotNull {
                        getFieldValue(it, type)
                    }
                }
                .getOrElse { emptyList() }
    }

    private fun getFieldValue(it: Field, type: Any): Any? {
        it.isAccessible = true
        return it.get(type)
    }

    private fun collectUniqueTypesFrom(action: Sql?, joinsMap: MutableMap<*, *>): List<Any> {
        val allObjects = mutableSetOf<Any>()
        action?.t.toOpt()
                .map(allObjects::add)

        joinsMap.iterator()
                .toOpt()
                .lmap { entry: MutableMap.MutableEntry<Any, Any> -> addUniques(entry, allObjects) }

        return allObjects.distinctBy(UidBuilder::buildUniqueId)
    }

    private fun addUniques(entry: MutableMap.MutableEntry<Any, Any>, uniques: MutableSet<Any>) {
        when (val e = entry.value) {
            is List<*> -> addUniques(e, uniques)
            else -> uniques.add(e)
        }
    }

    private fun addUniques(list: List<*>, uniques: MutableSet<Any>) {
        list.forEach {
            when (it) {
                is List<*> -> addUniques(it, uniques)
                else -> it?.let(uniques::add)
            }
        }
    }
}