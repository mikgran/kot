package mg.util.common

import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.functional.toOpt
import java.lang.reflect.Field
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

    fun isList(field: Field) = List::class.java.isAssignableFrom(field.type)
    private fun isKotlinType(field: Field) = field.type.packageName.contains("kotlin.") // or startsWith
    private fun isJavaType(field: Field) = field.type.packageName.contains("java.")

    // FIXME: -1 add testing, doesn't take primitives into count
    fun isCustom(field: Field) = (!(::isList or ::isKotlinType or ::isJavaType))(field)

    fun isCustomThatContainsCustoms(obj: Any): Boolean {
        val fields = obj::class.java.declaredFields.toCollection(ArrayList())
        val isAnyFieldCustom = fields.any(::isCustom)
        val isCustomInsideAnyListsFirstElement = fields.any { it.isListOfCustoms(obj) }

        return isAnyFieldCustom || isCustomInsideAnyListsFirstElement
    }

    fun hasCustomPackageName(obj: Any): Boolean {
        val lowerCasePackageName = obj::class.java.packageName.lowercase()
        return listOf("kotlin.", "java.").none(lowerCasePackageName::contains)
    }
}

fun <T : Any> Field.isCustom(ownerOfField: T): Boolean =
        this.also { isAccessible = true }
                .get(ownerOfField)
                .toOpt()
                .filter(Common::hasCustomPackageName)
                .isPresent()

fun <T : Any> Field.isType(ownerOfField: T, type: KClass<*>): Boolean =
        this.also { isAccessible = true }
                .get(ownerOfField)
                .toOpt()
                .mapTo(type)
                .isPresent()

fun <T : Any> Field.isListOfCustoms(ownerOfField: T): Boolean =
        this.also { isAccessible = true }
                .get(ownerOfField)
                .toOpt()
                .mapTo(List::class)
                .filter(List<*>::isNotEmpty)
                .map(List<*>::first)
                .filter(Common::hasCustomPackageName)
                .isPresent()

operator fun StringBuilder.plus(s: String): StringBuilder = append(s)
operator fun StringBuilder.plus(sb: StringBuilder): StringBuilder = append(sb.toString())