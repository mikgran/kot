package mg.util.db

interface SqlMapper {

    // DDL, DML
    // CREATE, SELECT, UPDATE, DELETE, ALTER, RENAME, TRUNCATE

    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String

    fun <T : Any> buildInsert(metadata: Metadata<T>): String

    fun <T : Any> buildFind(metadata: Metadata<T>): String

}
