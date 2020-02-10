package mg.util.db.dsl

import mg.util.common.plus
import mg.util.db.dsl.Sql.Parameters
import mg.util.functional.Opt2.Factory.of

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
open class DslMapper {

    // OracleMapper() : DslMapper
    // MySqlMapper() : DslMapper
    // DB2Mapper() : DslMapper
    // TODO: 2 add all MySql mapping functions

    fun map(dsl: Sql): String {
        return of(dsl)
                .map(::build)
                .getOrElseThrow { Exception("map: Unable to build sql for dsl: $dsl") }!!
    }

    private fun build(sql: Sql): String {

        val p = sql.parameters()

        (p.joins.iterator() +
                p.wheres.iterator() +
                p.updates.iterator())
                .forEach { build(p, it) }

        return build(p, p.action)
    }

    private fun build(p: Parameters, sql: Sql?): String {
        return when (sql) {
            is Sql.Create,
            is Sql.Drop,
            is Sql.Select,
            is Sql.Insert,
            is Sql.Update -> sql.build(p)
            is Sql.Delete -> "" // TODO 1
            is Sql.Select.Join,
            is Sql.Select.Join.On,
            is Sql.Select.Join.On.Eq,
            is Sql.Select.Join.Where,
            is Sql.Select.Join.Where.Eq,
            is Sql.Select.Join.Where.Eq.Where,
            is Sql.Select.Join.Where.Eq.Where.Eq,
            is Sql.Select.Where,
            is Sql.Select.Where.Eq,
            is Sql.Update.Set,
            is Sql.Update.Set.Eq,
            is Sql.Update.Set.Eq.And,
            is Sql.Update.Set.Eq.And.Eq,
            is Sql.Update.Set.Eq.And.Eq.Where,
            is Sql.Update.Set.Eq.And.Eq.Where.Eq,
            is Sql.Update.Set.Eq.Where,
            is Sql.Update.Set.Eq.Where.Eq -> sql.build(p) // buildWhereEqPart(p, sql)
            null -> throw Exception("Action not supported: null")
        }
    }
}

class MySqlDslMapper : DslMapper()
class OracleDslMapper : DslMapper()