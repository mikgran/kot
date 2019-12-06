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

        val bb = getCustomObjects(dp)

        val cc = getListsOfCustomObjects(dp)
                .map { buildSqlForCustomCollections(dp, it) }

//        lists.flatten()
//                .filterNotNull()
//                .distinctBy { it::class.java.packageName + it::class.java.simpleName }
//                .apply(::println)
        return buildSqlForParent(dp)
    }

    private fun getCustomObjects(parentDslParameters: DslParameters): List<Any> {

        typeOfParent(parentDslParameters)
                .declaredFields
                .filterNot(::isList)

        return emptyList()
    }

    private fun typeOfParent(parentDslParameters: DslParameters): Class<out Any> =
            parentDslParameters.typeT!!::class.java

    private fun buildSqlForCustomCollections(parentDslParameters: DslParameters, listsOfCustomsObjects: List<*>): String {


        return ""
    }

    private fun getListsOfCustomObjects(dp: DslParameters): List<List<*>> {
        return typeOfParent(dp).declaredFields
                .filterNotNull()
                .filter(::isList)
                .map {
                    it.isAccessible = true
                    it.get(dp.typeT) as List<*>
                }
    }

    private fun isList(it: Field) = List::class.java.isAssignableFrom(it.type)

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
        return of(dp)
                .filter { dp.typeT != null }
                .map { dp.typeT as Any }
                .map { it::class.declaredMemberProperties }
                .map { it.map { i -> MySqlTypeMapper().getTypeString(i) } }
                .getOrElse(emptyList())
    }
}
