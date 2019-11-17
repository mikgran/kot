package mg.util.common

object Common {

    fun hasContent(candidate: String?): Boolean = when {
        candidate != null && candidate.isNotEmpty() -> true
        else -> false
    }

    fun hasContent(candidate: Any?): Boolean = when (candidate) {
        null -> false
        else -> true
    }

    fun nonThrowingBlock(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            // no operation
        }
    }


}

