package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.SQL2.Parameters
import mg.util.db.dsl.mysql.*
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
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

        info.joins.forEach { buildFragment(info, it) }
        info.wheres.forEach { buildFragment(info, it) }

        return buildFragment(info, info.action)
    }

    private fun buildFragment(info: Parameters, sql: SQL2?): String {
        return when (sql) {
            is SQL2.Select -> buildSelect(info, sql)
            is SQL2.Select.Join -> buildJoinFieldFragment(sql).apply { info.fieldFragments += this }
            is SQL2.Select.Join.Where,
            is SQL2.Update.Set.Where,
            is SQL2.Select.Where ->
                buildWhereFieldFragment(sql as SQL2.Select.Where)
                        .apply { info.whereFragments += this }
            is SQL2.Select.Join.Where.Eq,
            is SQL2.Update.Set.Where.Eq,
            is SQL2.Select.Where.Eq -> ""
            is SQL2.Update -> ""
            is SQL2.Update.Set -> ""
            is SQL2.Delete -> ""
            null -> throw Exception("Action not supported: null")
        }
    }

    private fun buildWhereFieldFragment(sql: SQL2.Select.Where): String {
        val uidt = UidBuilder.buildUniqueId(sql.t.javaClass.kotlin)
        return of(sql.t)
                .mapTo(KProperty1::class)
                .mapWith(uidt) { kp, uid -> "${uid}.${kp.name}" }
                .toString()
    }

    private fun buildSelect(info: Parameters, select: SQL2.Select?): String {
        info.tableFragments += buildTableFragment(select as SQL2)
        info.fieldFragments += buildJoinFieldFragment(select as SQL2)

        return StringBuilder().apply {
            append("SELECT ${info.fieldFragments.joinToString(",")}")
            append(" FROM ${info.tableFragments.joinToString(",")}")
            if (info.whereFragments.isNotEmpty()) {
                append(" WHERE ${info.whereFragments.joinToString(" AND ")}")
            }
        }.toString()
    }

    private fun buildTableFragment(sql: SQL2): String {
        val (uid, alias) = buildUidAndAlias(sql)
        return "$uid AS $alias"
    }

    private fun buildJoinFieldFragment(sql: SQL2): String {
        val (_, alias) = buildUidAndAlias(sql)
        return sql.t::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
    }

    private fun buildUidAndAlias(sql2: SQL2?): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(sql2?.t ?: "")
        val alias = AliasBuilder.build(uid)
        return uid to alias
    }

    //
    // Old functionality
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