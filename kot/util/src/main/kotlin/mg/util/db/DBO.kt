package mg.util.db

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslMapper
import mg.util.db.dsl.mysql.SelectBlock
import mg.util.functional.Opt2.Factory.of
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberProperties

// a simple Object-Relational-Mapping class
class DBO(private val mapper: SqlMapper) {

    // Considerations:
    // - cache all Metadata objects?

    fun <T : Any> buildMetadata(type: T): Metadata<T> {

        val propertiesOfT: ArrayList<KCallable<*>> = propertiesOfT(type)
        val uid = UidBuilder.buildUniqueId(type)
        val name = type::class.simpleName ?: ""

        return Metadata(
                propertiesOfT.size,
                name,
                uid,
                type,
                propertiesOfT
        )
    }

    private fun <T : Any> propertiesOfT(t: T): ArrayList<KCallable<*>> {
        return t::class.memberProperties
                .toCollection(ArrayList())
    }

    fun <T : Any> save(t: T, connection: Connection) {

        val insertSql = of(t)
                .map(::buildMetadata)
                .map(mapper::buildInsert)
                .getOrElseThrow { Exception("$UNABLE_TO_BUILD_INSERT$t") }

        of(getStatement(connection))
                .map { s -> s.executeUpdate(insertSql) }
                .getOrElseThrow { Exception("$UNABLE_TO_DO_INSERT$t") }
    }

    private fun getStatement(connection: Connection): Statement? {
        return of(connection)
                .filter { !it.isClosed }
                .ifMissingThrow { Exception(CONNECTION_WAS_CLOSED) }
                .map(Connection::createStatement)
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_STATEMENT) }
    }

    fun <T : Any> ensureTable(t: T, connection: Connection) {

        val createTableSql = of(t)
                .map(::buildMetadata)
                .map(mapper::buildCreateTable)
                .getOrElseThrow { Exception(UNABLE_TO_BUILD_CREATE_TABLE) }

        of(getStatement(connection))
                .map { it.executeUpdate(createTableSql) }
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_TABLE) }
    }

    fun findBy(block: BuildingBlock, connection: Connection): List<Any> {

        val list = block.list()

        val type = of(list)
                .filter { it.isNotEmpty() }
                .map { it[0] }
                .mapTo(SelectBlock::class)
                .map { it.type }
                .get()

        val sql = DslMapper.map(list)

        @Suppress("UNCHECKED_CAST")
        return of(getStatement(connection))
                .mapWith(sql) { c, s -> c.executeQuery(s) }
                .filter(ResultSet::next)
                .mapWith(type) { rs, t -> ObjectBuilder().buildListOfT(rs, t) }
                .getOrElse(mutableListOf())
                .toList()
    }

    fun <T : Any> find(t: T, connection: Connection): List<T> {

        val findSql = of(t)
                .map(::buildMetadata)
                .map(mapper::buildFind)
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        val mappedT = of(getStatement(connection))
                .mapWith(findSql) { c, sql -> c.executeQuery(sql) }
                .filter(ResultSet::next)
                .mapWith(t) { rs, type -> ObjectBuilder().buildListOfT(rs, type) }
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        return mappedT ?: emptyList()
    }

    fun <T : Any> drop(t: T, connection: Connection) {

        val dropSql = of(t)
                .map(::buildMetadata)
                .map(mapper::buildDrop)
                .get()

        of(connection)
                .map(Connection::createStatement)
                .mapWith(dropSql) { s, sql -> s.executeUpdate(sql) }
    }

//    fun <T : Any> buildUniqueId(t: T): String {
//        return UidBuilder.buildUniqueId(t)
//    }

    companion object {
        const val CONNECTION_WAS_CLOSED = "Connection was closed while attempting to read from it"
        const val UNABLE_TO_BUILD_INSERT = "Unable to build insert from: "
        const val UNABLE_TO_DO_INSERT = "Unable to save an object: "
        const val UNABLE_TO_DO_FIND = "Unable to find an object: "
        const val UNABLE_TO_CREATE_STATEMENT = "Unable to build create statement"
        const val UNABLE_TO_BUILD_CREATE_TABLE = "Unable to build create table"
        const val UNABLE_TO_CREATE_TABLE = "Unable to create a new table"
        // const val UNABLE_TO_DO_DSL_FIND = "Unable to find an object with: "
    }

}