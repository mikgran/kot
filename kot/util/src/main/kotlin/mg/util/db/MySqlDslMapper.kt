package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object MySqlDslMapper : DslMapper {

    private val dbConfig = DBConfig(Config())
    private val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))

    override fun map(blockList: MutableList<BuildingBlock>): String {
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
            else -> throw Exception("buildSql: Class ${blocks[0]::class} not yet implemented")
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
                .getOrElseThrow { Exception("buildSelect: Cannot build uid for ${select.type}") }!!
        /*
           private val columns = HashMap<String, String>()
           private val tables = HashMap<String, String>()
           private val wheres = HashMap<String, String>()

           val op = Sql select PersonB() where PersonB::firstName eq "name"
           // SELECT * FROM person12345 as p WHERE p.firstName = "name"
        */
        val uidAlias = AliasBuilder.alias(uniqueId)

        val fields = typeT::class.memberProperties.joinToString(", ") { p -> "$uidAlias.${p.name.toString()}" }

        val operations = "$uidAlias.${where.type.name}"

        println("operations: $operations")

        val compares =

        // SELECT p.firstName, p.lastName, p2.firstName, p2.lastName, p2.age FROM person12345 p, person1234567 p2 WHERE p.firstName = "name"
        return "SELECT $fields FROM $uniqueId $uidAlias WHERE $operations"
    }

}
