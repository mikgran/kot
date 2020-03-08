package mg.util.db.dsl

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class MySqlTypeMapper {

    // TODO 1 add remaining missing type mappings
    open fun getTypeString(type: KProperty1<out Any, Any?>): String {

        val typeT = type.returnType.classifier as KClass<*>

        return when {
            typeT.simpleName == "Int" && type.name == "id" -> "${type.name} MEDIUMINT(9) NOT NULL AUTO INCREMENT PRIMARY KEY"
            typeT.simpleName == "String" -> "${type.name} VARCHAR(64) NOT NULL"
            typeT.simpleName == "Int" -> "${type.name} MEDIUMINT NOT NULL"
            else -> ""
        }

    }

//    private fun buildCustom(typeT: KClass<*>): String {
//
//        // In the root class
//        // notKotlinOrJavaType -> fieldNameRefId
//        // list -> fieldNameRefId
//        // in root class -> ignore
//        // in referred class -> add as RefId
//
//    }
}