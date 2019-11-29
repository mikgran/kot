package mg.util.db

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType

object MySqlTypeMapper {

    fun getTypeString(type: KProperty1<out Any, Any?>): String {

        val typeT = type.returnType.classifier as KClass<*>

        return when {
            typeT.simpleName == "Int" && type.name == "id" -> "${type.name} MEDIUMINT(9) NOT NULL AUTO INCREMENT PRIMARY KEY"
            typeT.simpleName == "String" -> "${type.name} VARCHAR(64) NOT NULL"
            typeT.simpleName == "Int" -> "${type.name} MEDIUMINT(9) NOT NULL"
            else -> buildReferenceIdForCustomObject(typeT)
        }

    }

    private fun buildReferenceIdForCustomObject(typeT: KClass<*>): String {
        val uid = UidBuilder.build(typeT)
        return "${uid}id MEDIUMINT(9)"
    }

}