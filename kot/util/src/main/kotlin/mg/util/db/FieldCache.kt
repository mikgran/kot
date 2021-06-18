package mg.util.db

import mg.util.common.Cache
import mg.util.common.isCustom
import mg.util.common.isListOfCustoms
import mg.util.common.isType
import java.lang.reflect.Field
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class FieldCache {

    data class Fields(
            val all: MutableList<Field> = mutableListOf(),
            val customs: MutableList<Field> = mutableListOf(),
            val arrays: MutableList<Field> = mutableListOf(),
            val collections: MutableList<Field> = mutableListOf(),
            val listsOfCustoms: MutableList<Field> = mutableListOf(),
            val primitives: MutableList<Field> = mutableListOf(),
    )

    companion object {

        internal val cache = Cache.of<Any, Fields>()

        fun <T : Any> fieldsFor(typeT: T): Fields {
            val uid = UidBuilder.buildUniqueId(typeT)
            return cache.getOrCache(uid) { collectMembers(typeT) }
        }

        private fun <T : Any> collectMembers(typeT: T): Fields {

            val allMembers = typeT::class.memberProperties.mapNotNull { it.javaField }
            val collectionMembers = allMembers.filter { it.isType(typeT, Collection::class) }
            val arrayMembers = allMembers.filter { it.isType(typeT, Array::class) }
            val listMembers = collectionMembers.filter { it.isListOfCustoms(typeT) }
            val customMembers = allMembers.minus(listMembers).filter { it.isCustom(typeT) }
            val primitiveMembers =
                    allMembers.minus(collectionMembers)
                            .minus(customMembers)
                            .minus(arrayMembers)

            return Fields().also {
                it.all.addAll(allMembers)
                it.customs.addAll(customMembers)
                it.listsOfCustoms.addAll(listMembers)
                it.primitives.addAll(primitiveMembers)
                it.arrays.addAll(arrayMembers)
                it.collections.addAll(collectionMembers)
            }
        }
    }
}
