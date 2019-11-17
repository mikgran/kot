package mg.util.db

import mg.util.functional.Opt2.Factory.of

/*
    map {
        l = map {
          lastName = Alias("l")
          lastDate = Alias("l2")
        }
        f = map {
          firstName = Alias("f")
        }
    }
*/
object AliasBuilder {

    internal data class Alias(var c: String, var i: Int = 1) {
        override fun toString(): String = if (i <= 1) c else "$c$i"
    }

    private val aliases = HashMap<String, HashMap<String, Alias>>()

    fun alias(s: String): String {

        val firstLetter = of(s)
                .filter(String::isNotEmpty)
                .ifMissingThrow { Exception("alias: Not possible to alias empty strings") }
                .map { "${s[0]}".toLowerCase() }
                .getOrElse("")

        val alias = of(aliases)
                .filter { it.containsKey(firstLetter) }
                .map { it[firstLetter] as HashMap<String, Alias> }
                .ifEmpty { newLetterMap(firstLetter) }
                .case({ it.containsKey(s) }, { it[s] as Alias })
                .case({ !it.containsKey(s) }, { newCachedAlias(firstLetter, s) })
                .result()

        return alias.toString()
    }

    private fun newLetterMap(firstLetter: String): HashMap<String, Alias> {
        val newLetterMap = HashMap<String, Alias>()
        aliases[firstLetter] = newLetterMap
        return newLetterMap
    }

    private fun newCachedAlias(firstLetter: String, s: String): Alias {
        val newAlias = Alias(firstLetter, aliases[firstLetter]!!.size + 1)
        aliases[firstLetter]!![s] = newAlias
        return newAlias
    }

    override fun toString(): String {
        return aliases.toSortedMap().toString()
    }

    internal fun aliases(): Map<String, HashMap<String, Alias>> {
        return aliases.toMap()
    }
}