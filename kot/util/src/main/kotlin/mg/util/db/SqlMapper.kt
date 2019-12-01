package mg.util.db

import mg.util.db.dsl.DslMapper.map
import mg.util.db.dsl.Sql

// mysql dialect object to sql mapper
class SqlMapper(internal val sql: Sql) {

    fun <T : Any> buildFind(metadata: Metadata<T>): String {
        val sql = sql select metadata.type
        return map(sql.list())
    }

    fun <T : Any> buildDrop(metadata: Metadata<T>): String {
        val sql = sql drop metadata.type
        return map(sql.list())
    }

    fun <T : Any> buildInsert(metadata: Metadata<T>): String {
        val sql = sql insert metadata.type
        return map(sql.list())
    }

    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {
        val sql = sql create metadata.type
        return map(sql.list())
    }
}