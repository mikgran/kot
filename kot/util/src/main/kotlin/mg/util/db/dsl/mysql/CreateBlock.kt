package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.full.declaredMemberProperties

open class CreateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildCreate(dp: DslParameters): String {

        // type (f1, f2, f3, custom1, collection<custom2>)
        // create type (f1, f2, f3), buildForParent(custom1), buildForParent(collection<custom2>)

        val lists = dp.typeT!!::class.java.declaredFields
                .filterNotNull()
                .filter { List::class.java.isAssignableFrom(it.type) }
                .map {
                    it.isAccessible = true
                    it.get(dp.typeT) as List<*>
                }
                .flatten()
                .filterNotNull()
                .distinctBy { it::class.java.packageName + it::class.java.simpleName }
                //.distinctBy { it::class.java.simpleName }
                .apply(::println)

        return createSimple(dp)
    }

    private fun createSimple(dp: DslParameters): String {
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
