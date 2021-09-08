package mg.util.db

import mg.util.common.Common.classSimpleName
import mg.util.db.dsl.FieldAccessor

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TableDrop {

    private val tablesToDrop = mutableListOf<Any>()
    private val joinTablesToDrop = mutableListOf<Pair<Any, Any>>()

    internal fun contentsTables(): MutableList<Any> {
        tablesToDrop.sortBy { it.classSimpleName() }
        return tablesToDrop
    }

    internal fun contentsJoinTables(): MutableList<Pair<Any, Any>> {
        joinTablesToDrop.sortBy { it.first.classSimpleName() + it.second.classSimpleName() }
        return joinTablesToDrop
    }

    fun clean() = dropAll()

    fun dropAll() {
        TestSupport.dropTables(tablesToDrop)
        TestSupport.dropJoinTables(joinTablesToDrop)
    }

    fun <T : Any, V : Any> registerJoin(pair: Pair<T, V>): Pair<T, V> =
            pair.also { joinTablesToDrop += it }

    fun <T : Any, V : Any> registerJoin(obj1: T, obj2: V): Pair<T, V> =
            registerJoin(obj1 to obj2)

    fun <T : Any> register(obj: T): T =
            obj.also { tablesToDrop += it }

    fun registerRelational(any: Any) {

        val uniquesByParent = FieldAccessor.uniqueChildrenByParent(any)
        val uniques: MutableSet<Any> = hashSetOf()
        uniques.addAll(uniquesByParent.keys)
        uniques.addAll(uniquesByParent.values.flatten())
        tablesToDrop.addAll(uniques)

        uniquesByParent.entries.forEach { entry ->
            entry.value.forEach { value ->
                joinTablesToDrop.add(entry.key to value)
            }
        }
    }
}
