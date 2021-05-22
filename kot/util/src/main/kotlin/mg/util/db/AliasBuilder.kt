package mg.util.db

import mg.util.functional.toOpt

/*
    map {
        "l" = map {
          "lastName" = Alias("l")
          "lastDate "= Alias("l2")
          "lostDate" = Alias("l3")
        }
        "f" = map {
          "firstName" = Alias("f")
        }
    }
*/
object AliasBuilder {

    internal data class Alias(var c: String, var i: Int = 1) {
        override fun toString(): String = if (i <= 1) c else "$c$i"
    }

    private var aliases = HashMap<String, HashMap<String, Alias>>()

    @Synchronized
    fun build(s: String): String {

        val firstLetter = s.toOpt()
                .filter(String::isNotEmpty)
                .ifMissingThrow { Exception("Not possible to alias empty strings") }
                .map { "${s[0]}".lowercase() }
                .getOrElse { "" }

        val alias = aliases.toOpt()
                .map { it[firstLetter] }
                .ifEmpty { newCachedLetterMap(firstLetter) }
                .map { it[s] }
                .ifEmpty { newCachedAlias(firstLetter, s) }

        return alias.toString()
    }

    private fun newCachedLetterMap(firstLetter: String) =
            HashMap<String, Alias>()
                    .also { aliases[firstLetter] = it }

    private fun newCachedAlias(firstLetter: String, s: String) =
            Alias(firstLetter, aliases[firstLetter]!!.size + 1)
                    .also { aliases[firstLetter]!![s] = it }

    override fun toString(): String {
        return aliases.toSortedMap().toString()
    }

    internal fun aliases(): Map<String, HashMap<String, Alias>> {
        return aliases.toMap()
    }
}