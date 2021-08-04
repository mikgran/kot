package mg.util.db.dsl

import mg.util.common.Cache
import mg.util.functional.toOpt

class IncrementalIdBuilder {

    private val cache = Cache.of<String, Int>()

    fun next(str: String): Int {
        return cache[str].toOpt()
                .ifEmpty { 0 }
                .map { it + 1 }
                .c { cache[str] = it }
                .value()
    }

    // return key + cache[key] or null if no key found
    fun id(key: String): String? =
            cache[key].toOpt()
                    .map { key + it }
                    .get()

    operator fun get(key: String): Int? = cache[key]

    internal fun contents() = cache
}