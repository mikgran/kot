package mg.util.common

open class Cache<T : Any, V : Any> private constructor() {

    private var lock = Any()
    private val cache = mutableMapOf<T, V>()

    internal fun cache() = synchronized(lock) { cache }

    fun getOrCache(key: T, valueSupplier: () -> V): V {
        return synchronized(lock) {
            this[key] ?: (valueSupplier().also { this[key] = it })
        }
    }

    operator fun get(key: T): V? {
        return synchronized(lock) {
            cache[key]
        }
    }

    operator fun set(key: T, value: V) {
        synchronized(lock) {
            cache[key] = value
        }
    }

    override fun toString(): String {
        return cache.toString()
    }

    companion object {
        fun <T : Any, V : Any> of() = Cache<T, V>()
    }
}
