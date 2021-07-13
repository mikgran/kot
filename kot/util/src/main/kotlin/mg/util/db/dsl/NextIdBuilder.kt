package mg.util.db.dsl

import mg.util.common.Cache
import mg.util.functional.toOpt

class NextIdBuilder private constructor() {

    companion object {

        private val cache = Cache.of<String, Int>()

        fun build(str: String): String {
            return str + cache[str].toOpt()
                    .ifEmpty { 0 }
                    .map { it + 1 }
                    .c { cache[str] = it }
                    .toString()
        }

        operator fun get(key: String): Int? = cache[key]

        internal fun contents() = cache
    }
}