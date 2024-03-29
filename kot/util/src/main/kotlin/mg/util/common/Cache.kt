package mg.util.common

/**
 * A simple key to value caching class. Synonymous to getOrPut behaviour in
 * HashMap with a couple of exceptions:
 * <code>
 * - all methods are synchronized by default
 * - replacement of the given key to value is not allowed after caching
 * - if a replacement of key to value is required the whole cache has to be replaced
 * to discourage changing the values. Use HashMap instead?
 * </code>
 */
open class Cache<T : Any, V : Any> private constructor() {

    private var lock = Any()
    private val mutableMap = mutableMapOf<T, V>()

    internal fun contents() = synchronized(lock) { mutableMap }

    fun getOrCache(key: T, valueSupplier: () -> V): V {
        return synchronized(lock) {
            this[key] ?: (valueSupplier().also { this[key] = it })
        }
    }

    operator fun get(key: T): V? {
        return synchronized(lock) {
            mutableMap[key]
        }
    }

    internal operator fun set(key: T, value: V) {
        synchronized(lock) {
            mutableMap[key] = value
        }
    }

    fun replaceWith(replacementMap: MutableMap<T, V>) {
        synchronized(lock) {
            mutableMap.clear()
            mutableMap.putAll(replacementMap)
        }
    }

    override fun toString(): String {
        return synchronized(lock) { mutableMap.toString() }
    }

    companion object {
        fun <T : Any, V : Any> of() = Cache<T, V>()
    }
}
