@file:Suppress("MemberVisibilityCanBePrivate")

package mg.util.common

import mg.util.functional.toOpt

object Common {

    fun hasContent(candidate: String?): Boolean = when (candidate) {
        null -> false
        else -> candidate.isNotEmpty()
    }

    fun hasAnyContent(candidate: Any?): Boolean = when (candidate) {
        null -> false
        else -> true
    }

    fun hasContent(candidate: List<*>?) = when (candidate) {
        null -> false
        else -> candidate.isNotEmpty()
    }

    fun nonThrowingBlock(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            // no operation
        }
    }

    fun <T : Any> T.classSimpleName(): String = this::class.simpleName ?: ""

    @Suppress("unused")
    fun printClassSimpleNames(map: Map<Any, Any>) {
        map.entries.forEach { entry ->
            print("K: ${entry.key.classSimpleName()} V: ")
            entry.value.toOpt()
                    .mapTo(List::class)
                    .map(List<*>::flattenTo)
                    .x {
                        joinToString(", ") {
                            it.classSimpleName()
                        }.also(::print)
                        println()
                    }
        }
    }

    fun splitWithDelimiters(str: String, delimiters: List<String>): List<String> {
        val lines = mutableListOf<String>()
        var index = 0
        var previousIndex = 0

        while (index > -1) {
            index = str.indexOfAny(delimiters, index)
            if (index > -1) {
                lines += str.substring(previousIndex, index)
                previousIndex = index
                index++
            }
        }

        lines += if (index != previousIndex) str.substring(previousIndex) else str
        return lines
    }
}

// shameless rip from the net:
fun List<*>.flattenTo(toFlatList: MutableList<Any> = mutableListOf()): MutableList<Any> {
    this.filterNotNull()
            .forEach {
                when (it) {
                    !is List<*> -> toFlatList.add(it)
                    else -> it.flattenTo(toFlatList)
                }
            }
    return toFlatList
}

fun List<*>.indexValid(index: Int): Boolean = (index > -1 && index < this.size)

fun <E> List<E>.flatten(): List<Any?> =
        this.flatMap {
            (it as? List<*>)?.asSequence() ?: sequenceOf(it)
        }

operator fun StringBuilder.plus(s: String): StringBuilder = append(s)
operator fun StringBuilder.plus(sb: StringBuilder): StringBuilder = append(sb.toString())

