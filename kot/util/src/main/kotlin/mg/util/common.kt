package mg.util

import java.util.*

object Common {

    fun hasContent(candidate: String?): Boolean = when {
        candidate != null && candidate.isNotEmpty() -> true
        else -> false
    }


}

