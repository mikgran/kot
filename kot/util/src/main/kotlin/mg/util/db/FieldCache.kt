package mg.util.db

import mg.util.common.isCustom
import mg.util.common.isListOfCustoms
import mg.util.functional.toOpt
import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

class FieldCache {

    data class Fields(
            val customs: List<Field> = mutableListOf(),
            val listsOFCustoms: List<Field> = mutableListOf(),
            val primitives: List<Field> = mutableListOf(),
    )


    companion object {

        private val cache = HashMap<Any, Fields>()

        fun <T : Any> fieldsFor(typeT: T): Fields {
            return cache[typeT].toOpt()
                    .ifEmpty {
                        collectFields(typeT)
                                .also { cache[typeT] = it }
                    }
                    .getOrElse { Fields() }
        }

        private fun <T : Any> collectFields(typeT: T): Fields {

            val allMembers = typeT::class.memberProperties

            val lists = getListMembers(allMembers, typeT)
            val customs = getCustomMembers(allMembers, lists, typeT)
            val primitives =



            return Fields()
        }

        private fun getListMembers(allMembers: Collection<KProperty1<out Any, *>>, type: Any): List<KProperty1<out Any, *>> {
            return allMembers
                    .filter {
                        it.returnType.jvmErasure.isSubclassOf(List::class)
                    }.filter { kProperty1 ->
                        kProperty1.toOpt()
                                .map { it.javaField }
                                .filter { it.isListOfCustoms(type) }
                                .isPresent()
                    }
        }

        private fun getCustomMembers(allMembers: Collection<KProperty1<out Any, *>>, listMembers: List<KProperty1<out Any, *>>, type: Any): List<KProperty1<out Any, *>> {
            return allMembers.minus(listMembers)
                    .filter { kProperty1 ->
                        kProperty1.toOpt()
                                .map { it.javaField }
                                .filter { it.isCustom(type) }
                                .isPresent()
                    }
        }
    }
}
