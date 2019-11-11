package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2.Factory.of

class SqlDslMapper {

    private val dbConfig = DBConfig(Config())
    private val columns = HashMap<String, String>()
    private val tables = HashMap<String, String>()
    private val wheres = HashMap<String, String>()

    companion object {

        fun map(blockList: MutableList<BuildingBlock>): String {

            // SELECT * FROM person12345 as p WHERE p.firstName = 'name'
            // SELECT person12345.firstName, person12345.lastName WHERE person12345.firstName = 'name'

            of(blockList)
                    .filter { it.isNotEmpty() }
                    .ifMissingThrow { Exception("List of blocks was empty") }

            return of(SqlDslMapper())
                    .mapWith(blockList) { dsl, list -> dsl.build(list) }
                    .getOrElseThrow { Exception("Unable to build sql for list $blockList") } ?: ""
        }
    }

    private fun build(blocks: MutableList<BuildingBlock>): String {
        return when (blocks[0]) {
            is SelectBlock<*> -> buildSelect(blocks)
            // is UpdateBlock<*> -> throw Exception("<UpdateBlock not yet implemented>")
            else -> throw Exception("Class ${blocks[0]::class} not yet implemented")
        }
    }

    private fun buildSelect(blocks: MutableList<BuildingBlock>): String {

        /*
            private val columns = HashMap<String, String>()
            private val tables = HashMap<String, String>()
            private val wheres = HashMap<String, String>()

            val op = Sql select PersonB() where PersonB::firstName is "name"

            // SELECT * FROM person12345 as p WHERE p.firstName = "name"
         */

        val select = blocks[0] as SelectBlock<*>
        val where = blocks[1] as WhereBlock<*>
        val operation = blocks[2] as OperationBlock<*>

        val typeT = of(select.type).getOrElseThrow { Exception("buildSelect: Missing type") }!!

        // SELECT * FROM person12345 as p WHERE p.firstName = "name"

        val uniqueId = of(DBO(SqlMapperFactory.get(dbConfig.mapper)))
                .map { it.buildUniqueId(typeT) }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Cannot build uid for ${select.type}") }

        val fields = ""
        val uidAlias = ""
        val operations = ""

        val selectStr = "SELECT $fields FROM $uniqueId $uidAlias WHERE $operations"


        return ""
    }

    fun buildFields(dbo: DBO): Any {

    }
}
