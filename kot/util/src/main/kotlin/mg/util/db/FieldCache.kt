package mg.util.db

import mg.util.common.isCustom
import mg.util.common.isListOfCustoms
import mg.util.functional.toOpt
import java.lang.reflect.Field
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class FieldCache {

    data class Fields(
            val customs: MutableList<Field> = mutableListOf(),
            val listsOfCustoms: MutableList<Field> = mutableListOf(),
            val primitives: MutableList<Field> = mutableListOf(),
    )

    companion object {

        internal val cache = HashMap<Any, Fields>()

        fun <T : Any> fieldsFor(typeT: T): Fields {
            return cache[typeT].toOpt()
                    .ifEmpty {
                        collectFields(typeT)
                                .also { cache[typeT] = it }
                    }
                    .getOrElse { Fields() }
        }

        private fun <T : Any> collectFields(typeT: T): Fields {
            val allMembers = typeT::class.memberProperties.mapNotNull { it.javaField }
            val listMembers = allMembers.filter { it.isListOfCustoms(typeT) }
            val customMembers = allMembers.minus(listMembers).filter { it.isCustom(typeT) }
            val primitiveMembers = allMembers.minus(listMembers).minus(customMembers)

            return Fields().also {
                it.customs.addAll(customMembers)
                it.listsOfCustoms.addAll(listMembers)
                it.primitives.addAll(primitiveMembers)
            }
        }

    }
}
