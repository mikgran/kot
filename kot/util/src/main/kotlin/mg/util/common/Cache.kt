package mg.util.common

import mg.util.functional.toOpt

open class Cache<T : Any, V : Any> private constructor() {

    private var lock = Any()
    private val cache = mutableMapOf<T, V>()
    private var keyMapper: ((Any) -> T)? = null

    fun getOrElse(key: Any, valueSupplier: () -> V): V {
        return this[key] ?: valueSupplier()
                .also {
                    this[key] = it
                }
    }

    operator fun get(key: Any): V? {
        return synchronized(lock) {
            keyMapper.toOpt()
                    .map { it(key) }
                    .map { cache[it] }
                    .get() ?: cache[key]
        }
    }

    operator fun set(key: Any, value: V) {
        synchronized(lock) {
            keyMapper.toOpt()
                    .map { it(key) }
                    .ifPresent { cache[it] = value }
        }
    }

    fun keyMapper(mapper: ((Any) -> T)): Cache<T, V> {
        synchronized(lock) {
            keyMapper = mapper
        }
        return this
    }

    companion object {
        fun <T : Any, V : Any> of() = Cache<T, V>()
    }
}

