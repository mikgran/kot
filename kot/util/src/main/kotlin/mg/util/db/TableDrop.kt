package mg.util.db

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TableDrop {

    private val tablesToDrop = mutableListOf<Any>()
    private val joinTablesToDrop = mutableListOf<Pair<Any, Any>>()

    fun clean() = dropAll()

    fun dropAll() {
        TestSupport.dropTables(tablesToDrop)
        TestSupport.dropJoinTables(joinTablesToDrop)
    }

    fun <T: Any, V: Any> registerJoin(pair: Pair<T, V>): Pair<T, V> =
            pair.also { joinTablesToDrop += it }

    fun <T: Any, V: Any> registerJoin(obj1: T, obj2: V): Pair<T, V> =
            registerJoin(obj1 to obj2)

    fun <T: Any> register(obj: T): T =
            obj.also { tablesToDrop += it }

    fun register(tables: HashSet<Any>) {

    }
}