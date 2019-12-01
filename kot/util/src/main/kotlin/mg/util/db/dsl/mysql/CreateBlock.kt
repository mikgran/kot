package mg.util.db.dsl.mysql

import mg.util.common.Common
import mg.util.db.*
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.db.dsl.MySqlTypeMapper
import mg.util.functional.Opt2
import kotlin.reflect.full.declaredMemberProperties

open class CreateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {

    override fun buildCreate(dp: DslParameters): String {

        dp.typeT = Opt2.of(type)
                .getOrElseThrow { Exception("buildFields: Missing select type") }!!

        dp.uniqueId = Opt2.of(dp.typeT)
                .map(UidBuilder::buildUniqueId)
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $type") }!!

        dp.uniqueIdAlias = AliasBuilder.alias(dp.uniqueId!!)

        val sqlFieldDefinitionsCommaSeparated = Opt2.of(dp)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
        val createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    private fun buildSqlFieldDefinitions(dp: DslParameters): List<String> {
        return Opt2.of(dp)
                .filter { dp.typeT != null }
                .map { dp.typeT as Any }
                .map { it::class.declaredMemberProperties }
                .map { it.map(MySqlTypeMapper::getTypeString) }
                .getOrElse(emptyList())
    }



}
