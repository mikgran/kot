package mg.util.common

import kotlin.reflect.KClass

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

    fun <T : Any> T.classSimpleName(): String? = this::class.simpleName

    fun printSimpleNames(map: HashMap<Any, List<Any>>) {
        map.entries.forEach { entry ->
            print("K: ${entry.key.classSimpleName()} V: ")
            entry.value.joinToString(", ") { it.classSimpleName() ?: "null" }.also { println(it) }
        }
    }
}

fun <E> List<E>.flatten(): List<Any?> =
        this.flatMap {
            (it as? List<*>)?.asSequence() ?: sequenceOf(it)
        }

operator fun StringBuilder.plus(s: String): StringBuilder = append(s)
operator fun StringBuilder.plus(sb: StringBuilder): StringBuilder = append(sb.toString())
