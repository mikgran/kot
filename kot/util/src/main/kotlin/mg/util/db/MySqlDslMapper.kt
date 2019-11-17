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
    private data class SelectParameters(var selectBlock: SelectBlock<*>? = null,
                                        var valueBlock: ValueBlock<*>? = null,
                                        var whereBlock: WhereBlock<*>? = null,
                                        var innerJoinBlock: InnerJoinBlock<*>? = null,
                                        var typeT: Any? = null,
                                        var uniqueId: String? = null,
                                        var uniqueIdAlias: String? = null,
                                        var fields: String? = null,
                                        var operations: String? = null
    )

    private fun buildSelect(blocks: MutableList<BuildingBlock>): String {

        val p = getParameters(blocks)

        getTypeTUidAndAlias(p)
        getFields(p)
        getOperations(p)

        // Sql select PersonB where PersonB::firstName eq "name" join
        // Permission on Person::id eq Permission::person_id
        val builder = StringBuilder()
                .append("SELECT ")
                .append(p.fields)
                .append(" FROM ")
                .append(p.uniqueId)
                .append(" ")
                .append(p.uniqueIdAlias)

        of(builder)
                .filter { Common.hasContent(p.whereBlock) }
                .map { it.append(" WHERE ") }
                .map { it.append(p.operations) }

        return builder.toString()

        // return "SELECT $fields FROM $uniqueId $uidAlias WHERE $operations"
    }

    private fun getOperations(p: SelectParameters) {
        p.operations = "${p.uniqueIdAlias}.${p.whereBlock?.type?.name} = '${p.valueBlock?.type?.toString()}'"
    }

    private fun getFields(p: SelectParameters) {
        p.fields = p.typeT!!::class.memberProperties.joinToString(", ") { "${p.uniqueIdAlias}.${it.name}" }
    }

    private fun getTypeTUidAndAlias(p: SelectParameters) {
        p.typeT = of(p.selectBlock?.type)
                .getOrElseThrow { Exception("buildSelect: Missing type") }!!

        p.uniqueId = of(dbo)
                .map { it.buildUniqueId(p.typeT!!) }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildSelect: Cannot build uid for ${p.selectBlock?.type}") }!!

        p.uniqueIdAlias = AliasBuilder.alias(p.uniqueId!!)
    }

    private fun getParameters(blocks: MutableList<BuildingBlock>): SelectParameters {
        val p = SelectParameters()
        blocks.forEach { b ->
            when (b) {
                is SelectBlock<*> -> p.selectBlock = b
                is WhereBlock<*> -> p.whereBlock = b
                is ValueBlock<*> -> p.valueBlock = b
                is InnerJoinBlock<*> -> p.innerJoinBlock = b
            }
        }
        return p
    }

}