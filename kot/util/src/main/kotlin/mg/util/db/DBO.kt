package mg.util.db

import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.toOpt
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberProperties

// a simple Object-Relational-Mapping class
class DBO(private val mapper: DefaultDslMapper) {

    fun <T : Any> buildMetadata(type: T): Metadata<T> {

        val propertiesOfT: ArrayList<KCallable<*>> = propertiesOfT(type)
        val uid = buildUniqueId(type)
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
        return t::class.memberProperties.toCollection(ArrayList())
    }

    fun <T : Any> save(t: T, connection: Connection) {

        // Car(name: String, tires: List<Tire>, windows: List<Window>)
        // - Set (parent, child)
        // - start transaction
        // - collect a set of parent-child
        // - iterate over Set for inserts
        // --- two-way ResultSet
        // --- fetch parentId
        // --- save children with parentId
        // - finish transaction

        val insertSql = t.toOpt()
                .map(::buildMetadata)
                .map(mapper::buildInsert)
                .getOrElseThrow { Exception("$UNABLE_TO_BUILD_INSERT$t") }

        // insertSql?.split(";")?.forEach(::println)

        getStatement(connection).toOpt()
                .map { s -> s.executeUpdate(insertSql) }
                .getOrElseThrow { Exception("$UNABLE_TO_DO_INSERT$t") }
    }

    fun <T : Any> save2(t: T, connection: Connection) {

        t.toOpt()
                .map(this::buildRelationMap)

    }

    private fun <T : Any> buildRelationMap(t: T): HashMap<T, Set<Any>> {

        // Car(name: String, tires: List<Tire>, windows: List<Window>)
        // Tables: Car, Tire, Window, CarTire, CarWindow
        // Creates: --"--
        // car -> tire, car -> window
        // 1. insert into car -> carid
        // 2. insert into tire with carid
        // 3. insert into window with carid


        val relations = hashMapOf<T, Set<Any>>()

        t::class.memberProperties.toSet()


        return relations

    }

    private fun getStatement(connection: Connection): Statement? {
        return connection.toOpt()
                .filter { !it.isClosed }
                .ifMissingThrow { Exception(CONNECTION_WAS_CLOSED) }
                .map(Connection::createStatement)
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_STATEMENT) }
    }

    fun <T : Any> ensureTable(t: T, connection: Connection) {
        val createTable = t.toOpt().map(::buildMetadata)
                .map(mapper::buildCreateTable)

        createTable
                .map { it.split(";") }
                .ifMissingThrow { Exception(UNABLE_TO_BUILD_CREATE_TABLE) }
                .case({ it.size == 1 }, { singleUpdate(connection, it) })
                .case({ it.size > 1 }, { batchUpdate(connection, it) })
    }

    private fun batchUpdate(connection: Connection, listSqls: List<String>) {
        getStatement(connection).toOpt()
                .mapWith(listSqls) { stmt, sqls -> sqls.map { stmt.addBatch(it) }; stmt }
                .map { it.executeBatch() }
                .getOrElseThrow { Exception("$UNABLE_TO_DO_UPDATE $listSqls") }
    }

    private fun singleUpdate(connection: Connection, listSqls: List<String>) {
        getStatement(connection).toOpt()
                .mapWith(listSqls) { stmt, sqls -> stmt.executeUpdate(sqls[0]) }
                .getOrElseThrow { Exception("$UNABLE_TO_DO_UPDATE $listSqls") }
    }

    fun <T : Any> find(t: T, connection: Connection): List<T> {

        val findSql = t.toOpt()
                .map(::buildMetadata)
                .map(mapper::buildFind)
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        return of(getStatement(connection))
                .mapWith(findSql) { c, sql -> c.executeQuery(sql) }
                .filter(ResultSet::next)
                .mapWith(t) { rs, type -> ObjectBuilder().buildListOfT(rs, type) }
                .getOrElse { mutableListOf() }
    }

    fun <T : Any> drop(t: T, connection: Connection) {

        val dropSql = t.toOpt()
                .map(::buildMetadata)
                .map(mapper::buildDrop)
                .get()

        of(connection)
                .map(Connection::createStatement)
                .mapWith(dropSql) { s, sql -> s.executeUpdate(sql) }
    }

    internal fun <T : Any> showColumns(t: T, connection: Connection): List<String> {

        val showColumnsSql = t.toOpt()
                .map(::buildMetadata)
                .map(mapper::buildShowColumns)
                .get()

        return of(connection)
                .map(Connection::createStatement)
                .mapWith(showColumnsSql) { stmt, sql -> stmt.executeQuery(sql) }
                .filter(ResultSet::next)
                .map { resultSet -> ObjectBuilder().buildListOfT(resultSet, "") }
                .getOrElse { mutableListOf() }
    }

    companion object {
        const val CONNECTION_WAS_CLOSED = "Connection was closed while attempting to read from it"
        const val UNABLE_TO_BUILD_INSERT = "Unable to build insert from: "
        const val UNABLE_TO_DO_INSERT = "Unable to save an object: "
        const val UNABLE_TO_DO_FIND = "Unable to find an object: "
        const val UNABLE_TO_CREATE_STATEMENT = "Unable to build create statement"
        const val UNABLE_TO_BUILD_CREATE_TABLE = "Unable to build create table"
        const val UNABLE_TO_CREATE_TABLE = "Unable to create a new table"
        const val UNABLE_TO_DO_UPDATE = "Unable to do update: "
        // const val UNABLE_TO_DO_DSL_FIND = "Unable to find an object with: "
    }
}

