package mg.util.db.dsl

import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.flatten
import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import mg.util.db.dsl.FieldAccessor.Companion.hasCustomPackageName
import mg.util.functional.toOpt
import java.lang.reflect.Field
import kotlin.reflect.KClass

class FieldAccessor private constructor() {

    companion object {

        fun fieldGet(field: Field, type: Any?): Any {
            field.isAccessible = true
            return field.get(type)
        }

        fun fieldSet(field: Field, type: Any?, value: Any?) {
            field.isAccessible = true
            field.set(type, value)
        }

        fun uniquesByParent(t: Any, uniquesByParent: HashMap<Any, List<Any>> = HashMap()): HashMap<Any, List<Any>> {
            when (t) {
                is MutableList<*> ->
                    t.filterNotNull().forEach {
                        uniquesByParent(it, uniquesByParent)
                    }
                else ->
                    getChildren(t).also {

                        uniquesByParent[t] = it
                        uniquesByParent(it, uniquesByParent)
                    }
            }
            return uniquesByParent
        }

        private fun getChildren(obj: Any): List<Any> {
            val fields = FieldCache.fieldsFor(obj)
            val customs = fields.customs
                    .map { fieldGet(it, obj) }
            val listsOfCustoms = fields.listsOfCustoms
                    .map { fieldGet(it, obj) }
                    .flatten()
                    .filterNotNull()
                    .distinctBy(UidBuilder::buildUniqueId)

            return customs + listsOfCustoms
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
}

fun <T : Any> Field.isCustom(ownerOfField: T): Boolean =
        this.also { isAccessible = true }
                .get(ownerOfField)
                .toOpt()
                .filter(::hasCustomPackageName)
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
                .filter(::hasCustomPackageName)
                .isPresent()