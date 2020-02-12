package mg.util.db.dsl

import mg.util.common.plus
import mg.util.functional.Opt2.Factory.of

// DDL, DML
// CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE(remove all rows from table), DROP
// include methods for data migration
abstract class DslMapper {

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
                .forEach { getImplementationFor(it).build(p) }

        return getImplementationFor(p.action).build(p)
    }

    abstract fun getImplementationFor(sql: Sql?): Sql
}

open class MySqlDslMapper : DslMapper() {

    override fun getImplementationFor(sql: Sql?): Sql {
        return when (sql) {
            is Sql.Create -> MySqlImpl.Create(sql.t)
            is Sql.Drop -> MySqlImpl.Drop(sql.t)
            is Sql.Select -> MySqlImpl.Select(sql.t)
            is Sql.Insert -> MySqlImpl.Insert(sql.t)
            is Sql.Update -> MySqlImpl.Update(sql.t)
            is Sql.Delete -> MySqlImpl.Delete(sql.t)
            is Sql.Delete.Where -> MySqlImpl.Delete.Where(sql.t)
            is Sql.Delete.Where.Eq -> MySqlImpl.Delete.Where.Eq(sql.t)
            is Sql.Select.Join -> MySqlImpl.Select.Join(sql.t)
            is Sql.Select.Join.On -> MySqlImpl.Select.Join.On(sql.t)
            is Sql.Select.Join.On.Eq -> MySqlImpl.Select.Join.On.Eq(sql.t)
            is Sql.Select.Join.Where -> MySqlImpl.Select.Join.Where(sql.t)
            is Sql.Select.Join.Where.Eq -> MySqlImpl.Select.Join.Where.Eq(sql.t)
            is Sql.Select.Join.Where.Eq.Where -> MySqlImpl.Select.Join.Where.Eq.Where(sql.t)
            is Sql.Select.Join.Where.Eq.Where.Eq -> MySqlImpl.Select.Join.Where.Eq.Where.Eq(sql.t)
            is Sql.Select.Where -> MySqlImpl.Select.Where(sql.t)
            is Sql.Select.Where.Eq -> MySqlImpl.Select.Where.Eq(sql.t)
            is Sql.Update.Set -> MySqlImpl.Update.Set(sql.t)
            is Sql.Update.Set.Eq -> MySqlImpl.Update.Set.Eq(sql.t)
            is Sql.Update.Set.Eq.And -> MySqlImpl.Update.Set.Eq.And(sql.t)
            is Sql.Update.Set.Eq.And.Eq -> MySqlImpl.Update.Set.Eq.And.Eq(sql.t)
            is Sql.Update.Set.Eq.And.Eq.Where -> MySqlImpl.Update.Set.Eq.And.Eq.Where(sql.t)
            is Sql.Update.Set.Eq.And.Eq.Where.Eq -> MySqlImpl.Update.Set.Eq.And.Eq.Where.Eq(sql.t)
            is Sql.Update.Set.Eq.Where -> MySqlImpl.Update.Set.Eq.Where(sql.t)
            is Sql.Update.Set.Eq.Where.Eq -> MySqlImpl.Update.Set.Eq.Where.Eq(sql.t)
            null -> throw Exception("Action not supported: null")

        }
    }

}

class OracleDslMapper : MySqlDslMapper() // TODO 1 placeholder: fix dialect