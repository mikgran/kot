package mg.util.common

class Cache<T : Any> private constructor() {

    private val cache = mutableMapOf<String, T>()
    private var supplier: (() -> T)? = null

    operator fun get(index: String): T? {
        return cache[index] ?: supplier?.invoke()

    }

    companion object {

        fun <T : Any> cacheOf(t: (Cache<T>) -> Unit): Cache<T> {
            return Cache<T>().also(t)
        }

    }
}