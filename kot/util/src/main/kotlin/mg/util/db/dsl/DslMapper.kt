package mg.util.db.dsl

import mg.util.db.dsl.mysql.CreateBlock
import mg.util.db.dsl.mysql.SelectBlock
import mg.util.functional.Opt2.Factory.of

object DslMapper {

    fun map(blockList: MutableList<BuildingBlock>): String {

        return of(blockList)
                .filter { it.isNotEmpty() }
                .ifMissingThrow { Exception("map: List of blocks was empty") }
                .map(DslMapper::buildSql)
                .getOrElseThrow { Exception("map: Unable to build sql for list $blockList") } ?: ""
    }

    private fun buildSql(blocks: MutableList<BuildingBlock>): String {
        return when (blocks[0]) {
            is SelectBlock<*> -> buildSelectNew(blocks)
            is CreateBlock<*> -> buildCreate(blocks)
            // is SelectBlock<*> -> buildSelect(blocks)
            // is UpdateBlock<*> -> throw Exception("<UpdateBlock not yet implemented>")
            else -> throw Exception("Class ${blocks[0]::class} not yet implemented")
        }
    }

    private fun buildCreate(blocks: MutableList<BuildingBlock>): String {
        val dp = DslParameters()
        blocks.map { it.buildFields(dp)}
        return blocks.map { it.buildCreate(dp) }
                .fold("") { a, b -> a + b }
    }

    // Select, Where, Value
    // Select, Where, Value, Join, JoinValue, Join, JoinValue
    private fun buildSelectNew(blocks: MutableList<BuildingBlock>): String {

        val dp = DslParameters()

        dp.fields = blocks
                .map { it.buildFields(dp) }
                .filter { it.isNotEmpty() }
                .joinToString(", ")

        return blocks
                .map { it.buildSelect(dp) }
                .fold("") { a, b -> a + b }
    }
}