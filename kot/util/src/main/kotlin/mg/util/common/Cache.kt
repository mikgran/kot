package mg.util.common

import mg.util.functional.toBiOpt
import mg.util.functional.toOpt

open class Cache<T : Any, V : Any> private constructor() {

    private val cache = mutableMapOf<T, V>()
    private var keySupplier: (() -> T)? = null
    private var valueSupplier: (() -> V)? = null

    operator fun get(index: T): V? {
        val key = keySupplier.toOpt()
                .map { it() }

        return key.map { cache[it] }
                .ifEmptyUse(valueSupplier)
                .toBiOpt(key)
//                .ifPresent { v ->
//                    key.ifPresent { k -> cache[k] = v }
//                }

                .get()
    }

    operator fun set(t: T, value: V) {

    }

    companion object {
        fun <T : Any, V : Any> cacheOf(key: T, value: V): Cache1<T, V> {
            val c = Cache<T, V>()
            return Cache1(c)
        }
    }

    class Cache1<T : Any, V : Any>(private val cache: Cache<T, V>) {
        fun valueSupplier(supplier: (() -> V)): Cache2<T, V> {
            cache.valueSupplier = supplier
            return Cache2(cache)
        }
    }

    class Cache2<T : Any, V : Any>(private val cache: Cache<T, V>) {
        fun keySupplier(supplier: (() -> T)): Cache3<T, V> {
            cache.keySupplier = supplier
            return Cache3(cache)
        }
    }

    class Cache3<T : Any, V : Any>(private val cache: Cache<T, V>) {
        fun build(): Cache<T, V> = cache
    }
}

