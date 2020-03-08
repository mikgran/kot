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

        fun getFieldsWithCustoms(dp: DslParameters): List<Field> {
            return dp.typeT!!::class.java
                    .declaredFields
                    .filterNotNull()
                    .filter(::isCustom)
        }

        fun getFieldsWithListOfCustoms(dp: DslParameters): List<Field> {
            return dp.typeT!!::class.java
                    .declaredFields
                    .filterNotNull()
                    .filter(::isList)
                    .filter { isSingleTypeList(it, dp) } // multiple type lists not supported atm.
        }

        private fun isSingleTypeList(field: Field, dp: DslParameters): Boolean {
            return (fieldGet(field, dp.typeT) as List<*>)
                    .filterNotNull()
                    .distinctBy { i -> "${i::class.java.packageName}.${i::class.java.simpleName}" }
                    .size == 1
        }

    }
}