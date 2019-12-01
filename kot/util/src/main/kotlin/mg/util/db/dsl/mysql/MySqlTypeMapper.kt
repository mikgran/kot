package mg.util.db.dsl.mysql

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class MySqlTypeMapper {

    open fun getTypeString(type: KProperty1<out Any, Any?>): String {

        val typeT = type.returnType.classifier as KClass<*>

        return when {
            typeT.simpleName == "Int" && type.name == "id" -> "${type.name} MEDIUMINT(9) NOT NULL AUTO INCREMENT PRIMARY KEY"
            typeT.simpleName == "String" -> "${type.name} VARCHAR(64) NOT NULL"
            typeT.simpleName == "Int" -> "${type.name} MEDIUMINT NOT NULL"
            else -> ""
        }

    }
}