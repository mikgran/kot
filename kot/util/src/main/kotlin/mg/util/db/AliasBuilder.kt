package mg.util.db

import mg.util.common.Cache
import mg.util.functional.toOpt

/*
    Cache {
        "l" = Cache {
          "lastName" = Alias("l")
          "lastDate "= Alias("l2")
          "lostDate" = Alias("l3")
        }
        "f" = Cache {
          "firstName" = Alias("f")
        }
    }
*/
object AliasBuilder {

    internal data class Alias(var c: String, var i: Int = 1) {
        override fun toString(): String = if (i <= 1) c else "$c$i"
    }

    private var aliasCache: Cache<String, Cache<String, Alias>> = Cache.of()

    fun build(str: String): String {
        val firstLetter = getFirstLetter(str)
        val letterCache = aliasCache.getOrCache(firstLetter) { Cache.of() }
        return letterCache
                .getOrCache(str) { Alias(firstLetter, letterCache.cache().size + 1) }
                .toString()
    }

    private fun getFirstLetter(s: String): String = s.toOpt()
            .filter(String::isNotEmpty)
            .ifMissingThrow { Exception("Not possible to alias empty strings") }
            .map { "${s[0]}".lowercase() }
            .getOrElse { "" }

    override fun toString(): String {
        return aliases().toString()
    }

    internal fun aliases(): Map<String, Cache<String, Alias>> {
        return aliasCache.cache().toSortedMap()
    }

    internal fun aliasCache() = aliasCache
}