package mg.util.db.dsl

import mg.util.db.Metadata

// A collection of hard bolted Dsl to sql string conversions.
class SqlMapper(internal val sql: Sql) {

    private val mapper = DslMapper()

    fun <T : Any> buildFind(metadata: Metadata<T>): String = mapper.map(sql select metadata.type)
    fun <T : Any> buildDrop(metadata: Metadata<T>): String = mapper.map(sql drop metadata.type)
    fun <T : Any> buildInsert(metadata: Metadata<T>): String = mapper.map(sql insert metadata.type)
    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String = mapper.map(sql create metadata.type)
    // sql update metadata.type -> full update of all fields given based on ref fields
}