package mg.util.db.dsl

import mg.util.db.Metadata

// A collection of hard bolted Dsl to sql string conversions.
class SqlMapper(internal val sql: Sql, internal val mapper: String) {

    // TODO: -16 currently both old and new functionality supported
    private val dslMapper = DslMapperFactory.get(mapper)

    fun <T : Any> buildFind(metadata: Metadata<T>): String = dslMapper.map(SQL2 select metadata.type)
    fun <T : Any> buildDrop(metadata: Metadata<T>): String = dslMapper.map(SQL2 drop metadata.type)
    fun <T : Any> buildInsert(metadata: Metadata<T>): String = dslMapper.map(SQL2 insert metadata.type)
    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String = dslMapper.map(SQL2 create metadata.type)
    // sql update metadata.type -> full update of all fields given based on ref fields
}