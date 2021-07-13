package mg.util.db.dsl

import mg.util.common.Cache

class NextIdBuilder private constructor() {

    companion object {

        private val cache = Cache.of<String, Int>()

        fun build(str: String): String {



            return ""
        }

        internal fun contents() = cache
    }
}