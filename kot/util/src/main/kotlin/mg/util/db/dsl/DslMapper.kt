package mg.util.db.dsl

import mg.util.db.dsl.mysql.*
import mg.util.functional.Opt2.Factory.of

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
object DslMapper {

    fun map(block: BuildingBlock): String = map(block.list())

    fun map(blockList: MutableList<BuildingBlock>): String {
        return of(blockList)
                .filter { it.isNotEmpty() }
                .ifMissingThrow { Exception("map: List of blocks was empty") }
                .map(::buildSql)
                .getOrElseThrow { Exception("map: Unable to build sql for list $blockList") } ?: ""
    }

    private fun buildSql(blocks: MutableList<BuildingBlock>): String {
        return when (blocks[0]) {
            is SelectBlock<*> -> buildSelectNew(blocks)
            is CreateBlock<*> -> buildCreate(blocks)
            is DropBlock<*> -> buildDrop(blocks)
            is InsertBlock<*> -> buildInsert(blocks)
            is UpdateBlock<*> -> buildUpdate(blocks)
            else -> throw Exception("Class ${blocks[0]::class} not yet implemented")
        }
    }

    private fun buildUpdate(blocks: MutableList<BuildingBlock>): String = build(blocks) { b, dp -> b.buildDelete(dp) }
    private fun buildInsert(blocks: MutableList<BuildingBlock>): String = build(blocks) { b, dp -> b.buildInsert(dp) }
    private fun buildDrop(blocks: MutableList<BuildingBlock>): String = build(blocks) { b, dp -> b.buildDrop(dp) }
    private fun buildCreate(blocks: MutableList<BuildingBlock>): String = build(blocks) { b, dp -> b.buildCreate(dp) }

    private fun build(blocks: MutableList<BuildingBlock>,
                      transformer: (BuildingBlock, DslParameters) -> String): String {
        val dp = DslParameters()
        blocks.map { it.buildFields(dp) }
        return blocks.map { transformer(it, dp) }
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