package mg.util.db

import mg.util.functional.Opt2
import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberProperties

// a simple Object-Relational-Mapping class
class DBO(val mapper: SqlMapper) {


    // ORM describe
    // Metadata:
    // - name of the object
    // - fields
    // - uid

    // Data:
    // - object name
    // - object fields
    // - object field data

    // Methods:
    // - build a deterministic unique name
    // - build definition of fields
    // - build access methods for each property

    // Requirements:
    // - don't use java: mg.util.db for any of the functions
    // - rewrite using kotlin reflection classes

    // Considerations:
    // - cache all Metadata objects?

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
        return t::class.memberProperties
                .toCollection(ArrayList())
    }

    fun <T : Any> buildUniqueId(t: T): String {

        val uid = Opt2.of(t)
                .map(::propertiesOfT)
                .filter { propertiesOfT -> propertiesOfT.size > 0 }
                .map { propertiesOfT -> propertiesOfT.filter { p -> p.name != "id" } }
                .map { propertiesOfT -> propertiesOfT.fold("") { n, p -> n + p.name } }
                .map { foldedNames -> t::class.simpleName + foldedNames.hashCode() }

        return uid.getOrElse("")
    }

    // TODO: create object? some other func name?
    fun <T : Any> save(t: T, connection: Connection) {

        val insertSql = Opt2.of(t)
                .map(::buildMetadata)
                .map(mapper::buildInsert)
                .getOrElseThrow { Exception("$UNABLE_TO_BUILD_INSERT$t") }

        Opt2.of(getStatement(connection))
                .map { s -> s.executeUpdate(insertSql) }
                .getOrElseThrow { Exception("$UNABLE_TO_DO_INSERT$t") }
    }

    private fun getStatement(connection: Connection): Statement? {
        return Opt2.of(connection)
                .filter { !it.isClosed }
                .ifMissingThrow { Exception(CONNECTION_WAS_CLOSED) }
                .map(Connection::createStatement)
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_STATEMENT) }
    }

    fun <T : Any> ensureTable(t: T, connection: Connection) {

        val createTableSql = Opt2.of(t)
                .map(::buildMetadata)
                .map(mapper::buildCreateTable)
                .getOrElseThrow { Exception(UNABLE_TO_BUILD_CREATE_TABLE) }

        Opt2.of(getStatement(connection))
                .map { s -> s.executeUpdate(createTableSql) }
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_TABLE) }
    }

    fun <T : Any> find(t: T, connection: Connection): List<T> {

        // TODO
        val findSql = Opt2.of(t)
                .map(::buildMetadata)
                .map(mapper::buildFind)
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        val mappedT = Opt2.of(getStatement(connection))
                .map { s -> s.executeQuery(findSql) }
                .filter(ResultSet::next)
                .map { rs -> map(t, rs) } // mapping function here
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        return Opt2.of(mappedT)
                .map { listOf(it) }
                .ifPresent (::println)
                .getOrElse(emptyList())
    }

    fun <T : Any> map(t: T, results: ResultSet?): T {

        // Person(firstName = "", lastName = "") -> find out which columns correspond to which object fields.
        val resultSet = Opt2.of(results)
                .ifPresent(ResultSet::beforeFirst)
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("No data in ResultSet to map") }

        // TODO when doing query manually, id included, when doing query based on object no id included so no minus 1
        val columnCountWithoutId = Opt2.of(results)
                .map(ResultSet::getMetaData)
                .map(ResultSetMetaData::getColumnCount)
                .map { it - 1 }
                .filter { it > 0 }
                .getOrElse(1)

        val constructor = Opt2.of(t::class.java.constructors)
                .map { c -> c.filter { it.parameterCount == columnCountWithoutId } }
                .filter { it.isNotEmpty() }
                .map { it[0] }
                .getOrElseThrow { Exception("No constructors in object ${t::class} to instantiate with") }

        val parameters = mutableListOf<Any>()
        (1..(resultSet?.metaData?.columnCount ?: 1)).forEach { i ->

            if (resultSet?.metaData?.getColumnName(i) != "id") {
                parameters.add(resultSet?.getString(i) as Any)
            }
        }

        val arrayAny = parameters.toTypedArray()

        val newInstance = Opt2.of(constructor)
                .map { it.newInstance(*arrayAny) }
                .getOrElseThrow { Exception("Unable to instantiate ${t::class}") }

        return newInstance as T // t as T
    }

    companion object {
        const val CONNECTION_WAS_CLOSED = "Connection was closed while attempting to read from it"
        const val UNABLE_TO_BUILD_INSERT = "Unable to build insert from: "
        const val UNABLE_TO_DO_INSERT = "Unable to save an object: "
        const val UNABLE_TO_DO_FIND = "Unable to find an object: "
        const val UNABLE_TO_CREATE_STATEMENT = "Unable to build create statement"
        const val UNABLE_TO_BUILD_CREATE_TABLE = "Unable to build create table"
        const val UNABLE_TO_CREATE_TABLE = "Unable to create a new table"
    }

}