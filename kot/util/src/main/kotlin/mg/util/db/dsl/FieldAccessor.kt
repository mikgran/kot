package mg.util.db.dsl

import mg.util.common.Common.isCustom
import mg.util.common.Common.isList
import java.lang.reflect.Field

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

        // TODO 900: replace with FieldCache.fieldsFor(typeT)
        fun getFieldsWithCustoms(dp: DslParameters): List<Field> =
                getFieldsWithCustoms(dp.typeT!!)

        fun getFieldsWithCustoms(type: Any): List<Field> =
                type::class.java
                        .declaredFields
                        .filterNotNull()
                        .filter(::isCustom)

        fun getFieldsWithListOfCustoms(dp: DslParameters): List<Field> =
                getFieldsWithListOfCustoms(dp.typeT!!)

        fun getFieldsWithListOfCustoms(type: Any): List<Field> =
                type::class.java
                        .declaredFields
                        .filterNotNull()
                        .filter(::isList)
                        .filter { isSingleTypeList(it, type) } // multiple type lists not supported atm.

        private fun isSingleTypeList(field: Field, type: Any): Boolean =
                (fieldGet(field, type) as List<*>)
                        .filterNotNull()
                        .distinctBy { i -> "${i::class.java.packageName}.${i::class.java.simpleName}" }
                        .size == 1

    }
}