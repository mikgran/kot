package mg.util.db

import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object MySqlTypeMapper {

    fun getTypeString(type: KProperty1<out Any, Any?>): String {

        val typeT = type.returnType.classifier as KClass<*>

        return when {
            typeT.simpleName == "Int" && type.name == "id" -> "${type.name} MEDIUMINT(9) NOT NULL AUTO INCREMENT PRIMARY KEY"
            typeT.simpleName == "String" -> "${type.name} VARCHAR(64) NOT NULL"
            typeT.simpleName == "Int" -> "${type.name} MEDIUMINT(9) NOT NULL"
            else -> buildReferenceIdForCustomObject(type, typeT)
        }

    }

    private fun buildReferenceIdForCustomObject(type: KProperty1<out Any, Any?>, typeT: KClass<*>): String {

        val uid = UidBuilder.build(typeT)

        return "${uid}id MEDIUMINT(9) ${typeT.}" // FIXME 3
    }

}