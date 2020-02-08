package mg.util.common

import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import java.lang.reflect.Field

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

    fun isList(field: Field) = List::class.java.isAssignableFrom(field.type)
    private fun isKotlinType(field: Field) = field.type.packageName.contains("kotlin.") // or startsWith
    private fun isJavaType(field: Field) = field.type.packageName.contains("java.")
    fun isCustom(field: Field) = (!(::isList or ::isKotlinType or ::isJavaType))(field)
}

operator fun StringBuilder.plus(s: String): StringBuilder = append(s)