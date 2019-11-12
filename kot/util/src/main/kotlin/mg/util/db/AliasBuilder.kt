package mg.util.db

import mg.util.functional.Opt2.Factory.of

object AliasBuilder {

    private data class Alias(var c: String, var i: Int = 1) {
        override fun toString(): String = if (i == 1) c else "$c$i"
    }

    private val aliases = HashMap<String, HashMap<String, Alias>>()

    /*
        map {
            l = map {
                lastName = Alias("l")
                lastDate = Alias("l2")
            }
        }
     */

    fun alias(s: String): String {

        val firstLetter = of(s)
                .filter(String::isNotEmpty)
                .ifMissingThrow { Exception("Not possible to alias empty strings") }
                .map { "${s[0]}" }
                .getOrElse("")

        var alias = ""

        of(aliases)
                .filter { it.containsKey(firstLetter) }
                .map { it[firstLetter] as HashMap<String, Alias> }
                .map { xxx }


        if (aliases.containsKey(firstLetter)) {

            val hashMap = aliases[firstLetter]
            if (true == hashMap?.containsKey(s)) {
                alias = hashMap[s].toString()
            } else {


            }


        } else {
            val hashMap = HashMap<String, Alias>()
            hashMap[s] = Alias(firstLetter)
            aliases[firstLetter] = hashMap
        }


        return ""
    }

}