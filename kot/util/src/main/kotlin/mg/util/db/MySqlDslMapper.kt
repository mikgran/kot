package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2.Factory.of

object MySqlDslMapper : DslMapper {

    private val dbConfig = DBConfig(Config())
    private val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))

    override fun map(blockList: MutableList<BuildingBlock>): String {

        // SELECT * FROM person12345 as p WHERE p.firstName = 'name'
        // SELECT person12345.firstName, person12345.lastName WHERE person12345.firstName = 'name'

        return of(blockList)
                .filter { it.isNotEmpty() }
                .ifMissingThrow { Exception("List of blocks was empty") }
                .map(::buildSql)
                .getOrElseThrow { Exception("Unable to build sql for list $blockList") } ?: ""
    }

    private fun buildSql(blocks: MutableList<BuildingBlock>): String {
        return when (blocks[0]) {
            is SelectBlock<*> -> buildSelect(blocks)
            // is UpdateBlock<*> -> throw Exception("<UpdateBlock not yet implemented>")
            else -> throw Exception("Class ${blocks[0]::class} not yet implemented")
        }
    }

    private fun buildSelect(blocks: MutableList<BuildingBlock>): String {

        val select = blocks[0] as SelectBlock<*>
        val where = blocks[1] as WhereBlock<*>
        val operation = blocks[2] as OperationBlock<*>

        val typeT = of(select.type)
                .getOrElseThrow { Exception("buildSelect: Missing type") }!!

        val uniqueId = of(dbo)
                .map { it.buildUniqueId(typeT) }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Cannot build uid for ${select.type}") }

        /*
           private val columns = HashMap<String, String>()
           private val tables = HashMap<String, String>()
           private val wheres = HashMap<String, String>()

           val op = Sql select PersonB() where PersonB::firstName eq "name"
           // SELECT * FROM person12345 as p WHERE p.firstName = "name"
        */

        val fields = ""

        val metadata = dbo.buildMetadata(typeT)


        val uidAlias = ""
        val operations = ""

        val selectStr = "SELECT $fields FROM $uniqueId $uidAlias WHERE $operations"


        return ""
    }

}
