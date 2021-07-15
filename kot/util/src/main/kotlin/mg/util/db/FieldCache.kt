package mg.util.db

import mg.util.common.Cache
import mg.util.db.dsl.isCustom
import mg.util.db.dsl.isListOfCustoms
import mg.util.db.dsl.isType
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
    ) {
        fun hasChildren() = customs.isNotEmpty() || listsOfCustoms.isNotEmpty()
    }

    companion object {

        internal val cache = Cache.of<Any, Fields>()

        fun <T : Any> fieldsFor(typeT: T): Fields {
            val uid = UidBuilder.buildUniqueId(typeT)
            return cache.getOrCache(uid) { collectMembers(typeT) }
        }

        private fun <T : Any> collectMembers(typeT: T): Fields {

            val all = typeT::class.memberProperties.mapNotNull { it.javaField }
            val collections = all.filter { it.isType(typeT, Collection::class) }
            val arrays = all.filter { it.isType(typeT, Array::class) }
            val listsOfCustoms = collections.filter { it.isListOfCustoms(typeT) }
            val customs = all.minus(collections).filter { it.isCustom(typeT) }
            val primitives =
                    all.minus(collections)
                            .minus(customs)
                            .minus(arrays)

            return Fields().also {
                it.all.addAll(all)
                it.customs.addAll(customs)
                it.listsOfCustoms.addAll(listsOfCustoms)
                it.primitives.addAll(primitives)
                it.arrays.addAll(arrays)
                it.collections.addAll(collections)
            }
        }
    }
}
