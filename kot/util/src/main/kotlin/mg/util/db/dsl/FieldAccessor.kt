package mg.util.db.dsl

import mg.util.common.Common.classSimpleName
import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.flatten
import mg.util.db.FieldCache
import mg.util.db.dsl.FieldAccessor.Companion.hasCustomPackageName
import mg.util.functional.mapIf
import mg.util.functional.toOpt
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

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

        fun uniqueChildrenByParent(type: Any) = childrenByParentImpl(type, ::getUniqueChildren)
        fun childrenByParent(type: Any) = childrenByParentImpl(type, ::getChildren)

        // parent -> childrenList
        // 0 -> 1, 2, 3, queue = 1, 2, 3
        // 1 -> 4, 5, queue =  2, 3, 4, 5
        // 2 -> 6, 7, queue = 3, 4, 5, 6, 7
        // queue = 4, 5, 6, 7
        // queue = 5, 6, 7
        // 5 -> 8, 9, queue = 6, 7, 8, 9
        // queue = 7, 8, 9
        // queue = 8, 9
        // queue = 9
        // queue = []
        private tailrec fun childrenByParentImpl(
                parent: Any?,
                childrenGetter: (Any) -> List<Any>,
                queue: ArrayDeque<Any> = ArrayDeque(),
                uniqueChildrenByParent: HashMap<Any, List<Any>> = LinkedHashMap(),
        ): HashMap<Any, List<Any>> {

            if (parent == null) {
                return uniqueChildrenByParent
            }

            val children = childrenGetter(parent)
            children.isNotEmpty().mapIf {
                uniqueChildrenByParent[parent] = children
                queue.addAll(children)
            }

            val newType = queue.isNotEmpty().mapIf {
                queue.remove()
            }

            return childrenByParentImpl(
                    newType.get(),
                    childrenGetter,
                    queue,
                    uniqueChildrenByParent
            )
        }

        private fun getChildrenImpl(obj: Any, buildLists: (List<Any>, List<Any>) -> List<Any>): List<Any> {
            val fields = FieldCache.fieldsFor(obj)
            val customs = fields.customs
                    .map { fieldGet(it, obj) }
            val listsOfCustoms = fields.listsOfCustoms
                    .map { fieldGet(it, obj) }
                    .flatten()
                    .filterNotNull()
            return buildLists(customs, listsOfCustoms)
        }

        private fun getUniqueChildren(obj: Any): List<Any> =
                getChildrenImpl(obj) { customs, listsOfCustoms ->
                    customs + (listsOfCustoms.distinctBy {
                        it.classSimpleName()
                    })
                }

        private fun getChildren(obj: Any): List<Any> =
                getChildrenImpl(obj) { customs, listsOfCustoms ->
                    customs + listsOfCustoms
                }

        fun isList(field: Field) = field.type.kotlin.isSubclassOf(List::class)
        private fun isKotlinType(field: Field) = "kotlin." in field.type.packageName // or startsWith
        private fun isJavaType(field: Field) = "java." in field.type.packageName
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
