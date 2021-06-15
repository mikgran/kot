package mg.util.common

import mg.util.functional.toOpt

open class Cache<T : Any, V : Any> private constructor() {

    private var lock = Any()
    internal val cache = mutableMapOf<T, V>()
    private var keyMapper: ((Any) -> T)? = null

    fun getOrCache(key: Any, valueSupplier: () -> V): V {
        return synchronized(lock) {
            this[key] ?: (valueSupplier().also { this[key] = it })
        }
    }

    operator fun get(key: Any): V? {
        return synchronized(lock) {
            keyMapper.toOpt()
                    .map { it(key) }
                    .map { cache[it] }
                    .get()
        }
    }

    operator fun set(key: Any, value: V) {
        synchronized(lock) {
            keyMapper.toOpt()
                    .map { it(key) }
                    .ifPresent { cache[it] = value }
        }
    }

    companion object {
        fun <T : Any, V : Any> of() = IncompleteInit(Cache<T, V>())
    }

    class IncompleteInit<T : Any, V : Any>(private val cache: Cache<T, V>) {
        fun keyMapper(mapper: ((Any) -> T)): Cache<T, V> {
            synchronized(cache.lock) {
                cache.keyMapper = mapper
            }
            return cache
        }
    }
}

