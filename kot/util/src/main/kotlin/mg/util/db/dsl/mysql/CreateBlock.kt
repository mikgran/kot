package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import java.lang.reflect.Field
import kotlin.reflect.full.declaredMemberProperties

open class CreateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildCreate(dp: DslParameters): String {

        // type (f1, f2, f3, custom1, collection<custom2>)
        // create type (f1, f2, f3), buildForParent(custom1), buildForParent(collection<custom2>)

        val customObjects = getCustomObjects(dp)

        val cc = getListsOfCustomObjects(dp)
                .map { buildSqlForCustomCollections(dp, it) }

        // println(cc)

        return buildSqlForParent(dp)
    }

    private fun isList(field: Field) = List::class.java.isAssignableFrom(field.type)
    private fun isKotlinPackage(field: Field) = field.type::class.java.packageName.contains("kotlin.")
    private fun isJavaPackage(field: Field) = field.type::class.java.packageName.contains("java.")

    private fun typeOfParent(parentDslParameters: DslParameters): Class<out Any> = parentDslParameters.typeT!!::class.java

    private fun buildSqlForCustomCollections(parentDslParameters: DslParameters, collectionField: Field): String {

        collectionField.isAccessible = true
        val collection = collectionField.get(parentDslParameters.typeT) as List<*>

        collection
                .filterNotNull()
                .distinctBy { "${it::class.java.packageName}.${it::class.java.simpleName}" }
        // .map { println(it); it }

        return ""
    }

    private fun getListsOfCustomObjects(parentDslParameters: DslParameters): List<Field> {
        // TODO: 8
        val listsWithCustoms = mutableListOf<Any>()

        val allLists = typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filter(::isList)
                .forEach { isEveryElementInListFieldTheSame(it, parentDslParameters) }
        
        return emptyList()
    }

    private fun isEveryElementInListFieldTheSame(field: Field, parentDslParameters: DslParameters) {
        field.isAccessible = true
        var firstElement: Any? = null
        of(field.get(parentDslParameters.typeT) as List<*>)
                .filter { it.isNotEmpty() }
                .ifPresent { firstElement = it[0] }
                .xmap { filterNotNull() }
                .xmap { all { it::class == firstElement!!::class } }
    }

    private fun getCustomObjects(parentDslParameters: DslParameters): List<Field> {
        return typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filterNot(::isList)
                .filterNot(::isKotlinPackage)
                .filterNot(::isJavaPackage)
    }

    private fun buildSqlForParent(dp: DslParameters): String {
        val sqlFieldDefinitionsCommaSeparated = of(dp)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
        val createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    private fun buildSqlFieldDefinitions(dp: DslParameters): List<String> {
        val mapper = MySqlTypeMapper()
        return of(dp)
                .filter { dp.typeT != null }
                .map { dp.typeT as Any }
                .map { it::class.declaredMemberProperties }
                .xmap { map(mapper::getTypeString).filter(String::isNotEmpty) }
                .getOrElse(emptyList())
    }
}
