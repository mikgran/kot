package mg.util.db

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

object TypeMapper {

    fun getTypeString(type: KProperty1<out Any, Any?>) : String {

        val kClass = type.returnType.classifier as KClass<*>
        return when (kClass.simpleName) {
            "String" -> "${type.name} VARCHAR(64) NOT NULL"
            else -> ""
        }
    }


}