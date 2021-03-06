package mg.util.db.dsl

import mg.util.db.Metadata

// A collection of fixed Dsl to Sql string methods.
class DefaultDslMapper(internal val mapper: String) {

    private val dslMapper = DslMapperFactory.get(mapper)

    fun <T : Any> buildFind(metadata: Metadata<T>): String = dslMapper.map(Sql select metadata.type)
    fun <T : Any> buildDrop(metadata: Metadata<T>): String = dslMapper.map(Sql drop metadata.type)
    fun <T : Any> buildInsert(metadata: Metadata<T>): String = dslMapper.map(Sql insert metadata.type)
    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String = dslMapper.map(Sql create metadata.type)
    // sql update metadata.type -> full update of all fields given based on ref fields

    // TODO: 1 finish me or finish sql insert method
    fun <T: Any> buildFindAll(metadata: Metadata<T>): String {
        return ""
    }

    fun <T: Any> buildInsertAll(metadata: Metadata<T>): String {
        return ""
    }

    internal fun <T: Any> buildShowColumns(metadata: Metadata<T>): String = dslMapper.map(Sql showColumns metadata.type)

}