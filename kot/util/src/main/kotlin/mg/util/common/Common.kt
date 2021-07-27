package mg.util.common

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
            when (entry.value) {
                is List<*> -> {
                    val list = entry.value as List<*>
                    if (list.isNotEmpty()) {
                        list.filterNotNull()
                                .joinToString(", ") { it.classSimpleName() }.also { println(it) }
                    }
                }
                else -> println(entry.value.classSimpleName())
            }
        }
    }
}


fun <E> List<E>.flatten(): List<Any?> =
        this.flatMap {
            (it as? List<*>)?.asSequence() ?: sequenceOf(it)
        }

operator fun StringBuilder.plus(s: String): StringBuilder = append(s)
operator fun StringBuilder.plus(sb: StringBuilder): StringBuilder = append(sb.toString())
