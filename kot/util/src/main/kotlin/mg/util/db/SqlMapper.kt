package mg.util.db

import mg.util.db.dsl.DslMapper.map
import mg.util.db.dsl.Sql

// A collection of hard bolted Dsl to sql string conversions.
class SqlMapper(internal val sql: Sql) {
    fun <T : Any> buildFind(metadata: Metadata<T>): String = map(sql select metadata.type)
    fun <T : Any> buildDrop(metadata: Metadata<T>): String = map(sql drop metadata.type)
    fun <T : Any> buildInsert(metadata: Metadata<T>): String = map(sql insert metadata.type)
    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String = map(sql create metadata.type)
    // sql update metadata.type -> full update of all fields given based on id
}