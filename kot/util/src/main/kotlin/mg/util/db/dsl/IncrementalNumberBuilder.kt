package mg.util.db.dsl

import mg.util.common.Cache
import mg.util.functional.toOpt

class IncrementalNumberBuilder {

    private val cache = Cache.of<String, Int>()

    fun next(str: String): Int =
            cache[str].toOpt()
                    .ifEmpty { 0 }
                    .map { it + 1 }
                    .c { cache[str] = it }
                    .value()

    fun inc(key: String): String = key + next(key)

    operator fun get(key: String): Int? = cache[key]

    internal fun cache() = cache
}