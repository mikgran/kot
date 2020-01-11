package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.SQL2.Parameters
import mg.util.db.dsl.mysql.*
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

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
                .map(::build)
                .getOrElseThrow { Exception("map: Unable to build sql for dsl: $dsl") }!!
    }

    private fun build(sql: SQL2): String {

        val parameters = sql.parameters()

        parameters.joins.forEach { build(parameters, it) }
        parameters.wheres.forEach { build(parameters, it) }
        parameters.updates.forEach { build(parameters, it) }

        return build(parameters, parameters.action)
    }

    private fun build(p: Parameters, sql: SQL2?): String {
        return when (sql) {
            is SQL2.Select -> buildSelect(p, sql)
            is SQL2.Select.Join -> buildAndAddJoinFieldAndTableFragments(p, sql)
            is SQL2.Select.Where,
            is SQL2.Select.Join.Where,
            is SQL2.Select.Join.Where.Eq.Where,
            is SQL2.Select.Join.Where.Eq.Where.Eq -> buildWhereFieldFragment(sql).also { p.whereFragments += it }
            is SQL2.Select.Where.Eq,
            is SQL2.Select.Join.Where.Eq -> buildEqFragment(sql).also { p.whereFragments += it }
            is SQL2.Delete -> ""
            is SQL2.Create -> buildCreate(p, sql)
            is SQL2.Update -> buildUpdate(p, sql)
            is SQL2.Update.Set,
            is SQL2.Update.Set.Eq.And -> buildUpdateFieldFragments(p, sql).also { p.updateFragments += it }
            is SQL2.Update.Set.Eq.Where,
            is SQL2.Update.Set.Eq.And.Eq.Where -> buildUpdateFieldFragments(p, sql).also { p.whereFragments += it }
            is SQL2.Update.Set.Eq,
            is SQL2.Update.Set.Eq.And.Eq -> buildEqFragment(sql).also { p.updateFragments += it }
            is SQL2.Update.Set.Eq.Where.Eq,
            is SQL2.Update.Set.Eq.And.Eq.Where.Eq -> buildEqFragment(sql).also { p.whereFragments += it }
            null -> throw Exception("Action not supported: null")
        }
    }

    private fun buildUpdateFieldFragments(p: Parameters, sql: SQL2): String =
            of(sql.t).mapTo(KProperty1::class)
                    .map { it.name }
                    .get()
                    .toString()

    private fun buildUpdate(p: Parameters, sql: SQL2): String {

        // "UPDATE $uid SET firstName = 'newFirstName', lastName = 'newLastName' WHERE firstName = 'firstName'"
        val builder = of(StringBuilder())
                .rcv {
                    append("UPDATE ")
                    append(UidBuilder.buildUniqueId(sql.t))
                    append(" SET ")
                    append(p.updateFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    if (p.whereFragments.isNotEmpty()) {
                        append(" WHERE ")
                        append(p.whereFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" })
                    }
                }

        return builder.get().toString()
    }

    private fun buildAndAddJoinFieldAndTableFragments(info: Parameters, sql: SQL2.Select.Join): String {
        info.fieldFragments += buildFieldFragment(sql)
        info.joinFragments += buildTableFragment(sql)
        return ""
    }

    private fun buildCreate(info: Parameters, sql: SQL2): String {
        return MySqlCreateBuilder().buildCreate(info, sql)
    }

    private fun buildEqFragment(sql: SQL2): String = of(sql.t).map { " = '$it'" }.toString()

    private fun buildWhereFieldFragment(sql: SQL2): String {

        val kProperty1 = of(sql.t).mapTo(KProperty1::class)

        val alias = kProperty1
                .map { it.javaField?.declaringClass?.kotlin }
                .map(UidBuilder::build)
                .map(AliasBuilder::build)

        return kProperty1
                .mapWith(alias) { p, a -> "${a}.${p.name}" }
                .toString()
    }

    private fun buildSelect(param: Parameters, select: SQL2.Select?): String {
        param.tableFragments.add(0, buildTableFragment(select as SQL2))
        param.fieldFragments.add(0, buildFieldFragment(select as SQL2))

        val whereStr = " WHERE "
        val whereFragmentsSize = param.whereFragments.size
        val whereElementCount = 2 // TOIMPROVE: add(Where(t)) add(Eq(t)) -> count == 2, distinctBy(t::class)?
        return of(StringBuilder())
                .rcv {
                    append("SELECT ${param.fieldFragments.joinToString(", ")}")
                    append(" FROM ${param.tableFragments.joinToString(", ")}")
                }
                .case({ whereFragmentsSize == whereElementCount }, { it.append(whereStr + param.whereFragments.joinToString("")); it })
                .case({ whereFragmentsSize / whereElementCount > 1 }, { it.append(whereStr + (param.whereFragments.chunked(2).joinToString(" AND ") { (i, j) -> "$i$j" })); it })
                .caseDefault { it }
                .result()
                .ifPresent {
                    if (param.joins.isNotEmpty()) {
                        it.append(" JOIN ")
                        it.append(param.joinFragments.joinToString(", "))
                    }
                }
                .get()
                .toString()
    }

    private fun buildTableFragment(sql: SQL2): String {
        val (uid, alias) = buildUidAndAlias(sql)
        return "$uid $alias"
    }

    private fun buildFieldFragment(sql: SQL2): String {
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