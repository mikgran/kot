package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.mysql.*
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

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

        return when (val action = info.action) {
            is SQL2.Select -> buildSelectFragment(info, action)
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
            else -> throw Exception("Action not supported: $action")
        }
    }

    private fun buildSelectFragment(info: SQL2.Parameters, select: SQL2.Select?): String {

        val uid = of(select)
                .map { it.t }
                .map { it::class.java.simpleName }
                .map { UidBuilder.buildUniqueId(it) }

        val alias = of(uid)
                .map { AliasBuilder.build(it) }

        // "SELECT $p.address, $p.rentInCents, $a.fullAddress FROM $p2 $p JOIN $a2 $a"

        val fields = of(info.joins)
                .lmap(this::buildTypeFields)

        val tables = of(info.joins)
                .mapWith(uid, alias) { joins, actionUid, actionAlias ->
                    var tables = "$actionUid $actionAlias "
                    val mappedJoins = joins.map { sql2: SQL2 ->
                        val (typeUid, typeAlias) = getUidAndAlias(sql2)
                        "$typeUid $typeAlias"
                    }
                    XXX
                    (listOf(tables) + mappedJoins).joinToString(",")
                }

        return "SELECT $fields FROM $tables"
    }

    private fun buildTypeFields(sql2: SQL2): String {
        val (_, alias) = getUidAndAlias(sql2)
        return sql2.t::class.declaredMemberProperties
                .joinToString(",") { p: KProperty1<out Any, Any?> ->
                    "${alias}.${p.name}"
                }
    }

    private fun getUidAndAlias(sql2: SQL2): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(sql2.t)
        val alias = AliasBuilder.build(uid)
        return uid to alias
    }

    //
    // Old functionality:
    //
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