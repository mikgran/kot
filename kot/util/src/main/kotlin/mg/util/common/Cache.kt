package mg.util.common

class Cache<T : Any, V : Any> private constructor() {

    private val cache = mutableMapOf<T, V>()
    private var supplier: (() -> V)? = null

    operator fun get(index: T): V? {
        return cache[index] ?: supplier?.invoke()
    }

    companion object {

        fun <T : Any, V : Any> cacheOf(t: (Cache<T, V>) -> Unit): Cache<T, V> {
            return Cache<T, V>().also(t)
        }

    }
}

