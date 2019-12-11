package mg.util.db.dsl.mysql

import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import java.lang.reflect.Field
import kotlin.reflect.full.declaredMemberProperties

open class CreateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildCreate(dp: DslParameters): String {

        // type (f1, f2, f3, custom1, collection<custom2>)
        // create type (f1, f2, f3), buildForParent(custom1), buildForParent(collection<custom2>)

        val customObjects = getFieldsWithCustoms(dp).map { println(fieldGet(it, dp.typeT!!));it }

        val cc = getFieldsWithListOfCustoms(dp).map { println(fieldGet(it, dp.typeT!!)); it }

        // buildSqlCreateWithRefField(dp.typeT as Any, customObjects.first())


        return buildSqlCreate(dp)
    }

    private fun fieldGet(it: Field, type: Any): Any {
        it.isAccessible = true
        return it.get(type)
    }

    private fun isList(field: Field) = List::class.java.isAssignableFrom(field.type)
    private fun isKotlinType(field: Field) = field.type.packageName.contains("kotlin.")
    private fun isJavaType(field: Field) = field.type.packageName.contains("java.")
    private fun typeOfParent(parentDslParameters: DslParameters): Class<out Any> = parentDslParameters.typeT!!::class.java

    private fun getFieldsWithListOfCustoms(parentDslParameters: DslParameters): List<Field> {
        return typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filter(::isList)
                .filter { isSingleTypeList(it, parentDslParameters) }
    }

    private fun isSingleTypeList(field: Field, dp: DslParameters): Boolean {
        field.isAccessible = true
        return (field.get(dp.typeT) as List<*>)
                .filterNotNull()
                .distinctBy { i -> "${i::class.java.packageName}.${i::class.java.simpleName}" }
                .size == 1
    }

    private fun getFieldsWithCustoms(parentDslParameters: DslParameters): List<Field> {
        return typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filter(::isCustom)
    }

    private fun isCustom(field: Field) = (!(::isList or ::isKotlinType or ::isJavaType))(field)

    private fun buildSqlCreate(dp: DslParameters): String {
        val sqlFieldDefinitionsCommaSeparated = of(dp.typeT)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
        val createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    private fun buildSqlFieldDefinitions(type: Any): List<String> {
        val mapper = MySqlTypeMapper()
        return of(type)
                .map { it::class.declaredMemberProperties }
                .xmap { map(mapper::getTypeString).filter(String::isNotEmpty) }
                .getOrElse(emptyList())
    }

//    private fun buildSqlCreateWithRefField(any: Any, first: Field) {
//
//        val sqlFieldDefinitionsCommaSeparated = of(dp)
//                .map(::buildSqlFieldDefinitions)
//                .map { it.joinToString(", ") }
//                .getOrElseThrow { Exception("Unable to build create") }
//
//        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
//        val createStringPostFix = ")"
//
//        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
//    }
}