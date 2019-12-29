package mg.util.db.dsl

import mg.util.db.dsl.mysql.*
import mg.util.functional.Opt2.Factory.of

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
open class DslMapper {

    // MySqlMapper.map(dsl: SQL) TODO -5 rename later
    // OracleMapper.map(dsl: SQl)
    // generic: DslMapper // TODO -6 generalize later
    // DslMapper::.buildSql(dsl: SQL)
    // val mapper: DslMapper = DslMapperFactory.dslMapper(name: String? = "MySql")
    // OracleMapper() : DslMapper
    // MySqlMapper() : DslMapper
    // DB2Mapper() : DslMapper
    // TODO: 12 add all MySql mapping functions

    fun map(dsl: SQL2): String {
        return of(dsl)
                .map(::buildSql2)
                .getOrElseThrow { Exception("map: Unable to build sql for dsl: $dsl") }!!
    }

    private fun buildSql2(sql: SQL2): String {

        val info = sql.parameters()

        return when (info.action!!) {
            is SQL2.Select -> ""
            is SQL2.Select.Join -> ""
            is SQL2.Select.Join.Where,
            is SQL2.Select.Join.Where.Eq,
            is SQL2.Select.Where,
            is SQL2.Select.Where.Eq -> ""
            is SQL2.Update -> ""
            is SQL2.Update.Set -> ""
            is SQL2.Update.Set.Where -> ""
            is SQL2.Update.Set.Where.Eq -> ""
            is SQL2.Update.delete -> ""
        }
    }

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
                      mapper: (BuildingBlock, DslParameters) -> String): String {

        val dp = blocks.first().buildDslParameters()
        return blocks.map { mapper(it, dp) }
                .fold("") { a, b -> a + b }
    }

    // Select, Where, Value
    // Select, Where, Value, Join, JoinValue, Join, JoinValue
    private fun buildSelectNew(blocks: MutableList<BuildingBlock>): String {

        val dp = blocks.first().buildDslParameters()
        dp.fields = blocks
                .map { it.buildFields(dp) }
                .filter { it.isNotEmpty() }
                .joinToString(", ")

        return blocks
                .map { it.buildSelect(dp) }
                .fold("") { a, b -> a + b }
    }
}

class MySqlDslMapper : DslMapper()
class OracleDslMapper : DslMapper()