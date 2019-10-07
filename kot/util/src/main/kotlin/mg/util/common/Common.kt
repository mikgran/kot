package mg.util.common

object Common {

    fun hasContent(candidate: String?): Boolean = when {
        candidate != null && candidate.isNotEmpty() -> true
        else -> false
    }

    fun nonThrowingBlock(block: () -> Unit)  {
        try {
            block()
        } catch (e: Exception) {
            // no operation
        }
    }


}

