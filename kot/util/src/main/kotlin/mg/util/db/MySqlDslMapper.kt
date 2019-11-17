package mg.util.db

import mg.util.common.Common
import mg.util.common.Common.hasContent
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
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
                                        var operations: String? = null,
                                        var joins: String? = null
    )

    private fun buildSelect(blocks: MutableList<BuildingBlock>): String {

        val p = getParameters(blocks)

        buildTypeTUidAndAlias(p)
        buildFields(p)
        buildOperations(p)
        buildJoins(p)

        // Sql select PersonB where PersonB::firstName eq "name" join
        // Permission on Person::id eq Permission::person_id
        val builder = of(StringBuilder())
                .rcv { append("SELECT ") }
                .rcv { append(p.fields) }
                .rcv { append(" FROM ") }
                .rcv { append(p.uniqueId) }
                .rcv { append(" ") }
                .rcv { append(p.uniqueIdAlias) }

        builder.filter { hasContent(p.whereBlock) }
                .rcv { append(" WHERE ") }
                .rcv { append(p.operations) }

        builder.filter { hasContent(p.innerJoinBlock) }
                .rcv { append(" JOIN ") }
                .rcv { append("") }

        return builder.get().toString()
    }

    private fun buildJoins(p: MySqlDslMapper.SelectParameters): String {

        // process a list of typeV joins
//        p.typeT = of(p.innerJoinBlock?.type)
//                .getOrElseThrow { Exception("buildSelect: Missing join type") }!!
//
//        p.uniqueId = of(dbo)
//                .map { it.buildUniqueId(p.typeT!!) }
//                .filter(Common::hasContent)
//                .getOrElseThrow { Exception("buildSelect: Cannot build uid for ${p.selectBlock?.type}") }!!
//
//        p.uniqueIdAlias = AliasBuilder.alias(p.uniqueId!!)


        return p.innerJoinBlock?.type!!::class.simpleName ?: ""
    }

    private fun buildOperations(p: SelectParameters) {
        p.operations = "${p.uniqueIdAlias}.${p.whereBlock?.type?.name} = '${p.valueBlock?.type?.toString()}'"
    }

    private fun buildFields(p: SelectParameters) {
        p.fields = p.typeT!!::class.memberProperties.joinToString(", ") { "${p.uniqueIdAlias}.${it.name}" }
    }

    private fun buildTypeTUidAndAlias(p: SelectParameters) {
        p.typeT = of(p.selectBlock?.type)
                .getOrElseThrow { Exception("buildSelect: Missing select type") }!!

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