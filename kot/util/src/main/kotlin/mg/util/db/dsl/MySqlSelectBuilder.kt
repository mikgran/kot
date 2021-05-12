package mg.util.db.dsl

import mg.util.common.Common
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.plus
import mg.util.functional.toOpt
import java.lang.reflect.Field

class MySqlSelectBuilder {

    fun build(p: Sql.Parameters, sql: Sql): String {

        val t = sql.t
        p.tableFragments.add(0, buildTableFragment(t))
        p.joinsMap.putAll(buildJoinsMap(t, p))

        p.toOpt()
                .map { MySqlImpl.buildJoinsForNaturalRefs(it) }
                .filter(String::isNotEmpty)
                .map(p.joinFragments::add)

        collectUniqueTypesFrom(p.action, p.joinsMap)
                .forEach { p.columnFragments += MySqlImpl.buildFieldPart(it) }

        val sb = StringBuilder() +
                "SELECT ${p.columnFragments.joinToString(", ")}" +
                " FROM ${p.tableFragments.joinToString(", ")}" +
                buildJoinPart(p) +
                buildManualJoinPart(p) +
                buildWherePart(p)

        return sb.toString()
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
        val (uid, alias) = MySqlImpl.buildUidAndAlias(type)
        return "$uid $alias"
    }

    private fun buildJoinsMap(root: Any, p: Sql.Parameters): MutableMap<Any, List<Any>> {

        val joinsMap = mutableMapOf<Any, List<Any>>()
        root.toOpt()
                .map {
                    val list: List<Any> = childrenForParent(it)
                    if (list.isNotEmpty()) {
                        joinsMap[it] = list
                    }
                    list
                }
                .xmap { forEach { buildJoinsMap(it, p) } }
        return joinsMap
    }

    private fun childrenForParent(type: Any): List<Any> {
        return type::class.java.declaredFields.toList()
                .toOpt()
                .lfilter(Common::isCustom or Common::isList)
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

    private fun collectUniqueTypesFrom(action: Sql?, joinsMap: MutableMap<*, *>): MutableSet<Any> {
        val uniques = mutableSetOf<Any>()
        action?.t.toOpt()
                .map(uniques::add)

        joinsMap.iterator()
                .toOpt()
                .lmap { entry: MutableMap.MutableEntry<Any, Any> -> getUniques(entry, uniques) }

        return uniques
    }

    private fun getUniques(entry: MutableMap.MutableEntry<Any, Any>, uniques: MutableSet<Any>) {
        when (val e = entry.value) {
            is List<*> -> getUniques(e, uniques)
            else -> uniques.add(e)
        }
    }

    private fun getUniques(list: List<*>, uniques: MutableSet<Any>) {
        list.forEach {
            when (it) {
                is List<*> -> getUniques(it, uniques)
                else -> it?.let(uniques::add)
            }
        }
    }
}