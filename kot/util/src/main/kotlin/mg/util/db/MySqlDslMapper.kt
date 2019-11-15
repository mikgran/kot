package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.full.memberProperties

object MySqlDslMapper : DslMapper {

    private val dbConfig = DBConfig(Config())
    private val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))

    override fun map(blockList: MutableList<BuildingBlock>): String {

        // SELECT * FROM person12345 as p WHERE p.firstName = 'name'
        // SELECT person12345.firstName, person12345.lastName WHERE person12345.firstName = 'name'

        return of(blockList)
                .filter { it.isNotEmpty() }
                .ifMissingThrow { Exception("map: List of blocks was empty") }
                .map(::buildSql)
                .getOrElseThrow { Exception("map: Unable to build sql for list $blockList") } ?: ""
    }

    private fun buildSql(blocks: MutableList<BuildingBlock>): String {
        return when (blocks[0]) {
            is SelectBlock<*> -> buildSelect(blocks)
            // is UpdateBlock<*> -> throw Exception("<UpdateBlock not yet implemented>")
            else -> throw Exception("Class ${blocks[0]::class} not yet implemented")
        }
    }

    /*
        val op = Sql select PersonB() where PersonB::firstName eq "name"
        // SELECT * FROM person12345 as p WHERE p.firstName = "name"
    */
    private fun buildSelect(blocks: MutableList<BuildingBlock>): String {

        val select = blocks[0] as SelectBlock<*>
        val where = blocks[1] as WhereBlock<*>
        val operation = blocks[2] as ValueBlock<*>

        val typeT = of(select.type)
                .getOrElseThrow { Exception("buildSelect: Missing type") }!!

        val uniqueId = of(dbo)
                .map { it.buildUniqueId(typeT) }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildSelect: Cannot build uid for ${select.type}") }!!

        val uidAlias = AliasBuilder.alias(uniqueId)

        val fields = typeT::class.memberProperties.joinToString(", ") { p -> "$uidAlias.${p.name}" }

        val operations = "$uidAlias.${where.type.name} = '${operation.type.toString()}'"

        // SELECT p.firstName, p.lastName FROM PersonB608543900 p WHERE p.firstName = 'name'
        // SELECT p.firstName, p.lastName, p2.name, p2.level FROM PersonB608543900 p WHERE p.firstName = 'name'
        // JOIN Permission12345 p2 ON p.id = p2.name
        // Sql select PersonB where PersonB::firstName eq "name" join
        // Permission on Person::id eq Persmission::name
        return "SELECT $fields FROM $uniqueId $uidAlias WHERE $operations"
    }

}