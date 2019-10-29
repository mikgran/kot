package mg.util.db

import mg.util.functional.Opt2
import java.lang.reflect.Constructor
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberProperties

// a simple Object-Relational-Mapping class
class DBO(val mapper: SqlMapper) {

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
                .filter { it.size > 0 }
                .map { it.filter { p -> p.name != "id" } }
                .map { it.fold("") { n, p -> n + p.name } }
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
                .map { it.executeUpdate(createTableSql) }
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_TABLE) }
    }

    fun <T : Any> find(t: T, connection: Connection): List<T> {

        val findSql = Opt2.of(t)
                .map(::buildMetadata)
                .map(mapper::buildFind)
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        val mappedT = Opt2.of(getStatement(connection))
                .map { it.executeQuery(findSql) }
                .filter(ResultSet::next)
                .map { rs -> map(t, rs) }
                .getOrElseThrow { Exception(UNABLE_TO_DO_FIND) }

        return mappedT ?: emptyList()
    }

    fun <T : Any> map(type: T, results: ResultSet?): List<T> {

        val columnCountWithoutId = getColumnCountWithoutId(results)

        val constructor = getConstructor(type, columnCountWithoutId)

        return ObjectBuilder().buildListOfT(results, constructor, type)
    }

    private fun getColumnCountWithoutId(results: ResultSet?): Int {
        // Person(firstName = "", lastName = "") -> find out which columns correspond to which object fields.
        val containsIdColumn = Opt2.of(results)
                .map(ResultSet::getMetaData)
                .filter { (1..it.columnCount).none { nr -> it.getColumnName(nr) == "id" } }
                .map { false } // ifPresent none contained
                .getOrElse(true) // !ifPresent at least one contained

        return Opt2.of(results)
                .map(ResultSet::getMetaData)
                .case({ containsIdColumn }, { it.columnCount - 1 })
                .case({ !containsIdColumn }, { it.columnCount })
                .right()
                .getOrElse(1)
    }

    private fun <T : Any> getConstructor(type: T, columnCountWithoutId: Int): Constructor<*>? {
        return Opt2.of(type::class.java.constructors)
                .map { c -> c.filter { it.parameterCount == columnCountWithoutId } }
                .filter { it.isNotEmpty() }
                .map { it[0] } // TOIMPROVE: add constructor parameter type checks, this just takes first with equal number of parameters
                .getOrElseThrow { Exception("No constructors in object ${type::class} to instantiate with") }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> buildListOfT(results: ResultSet?, constructor: Constructor<*>?, t: T): MutableList<T> {
        val listT = mutableListOf<T>()
        do {
            val parameters = mutableListOf<Any>()
            (1..(results?.metaData?.columnCount ?: 1)).forEach { i ->

                if (results?.metaData?.getColumnName(i) != "id") {
                    parameters.add(results?.getString(i) as Any)
                }
            }

            val arrayAny = parameters.toTypedArray()
            Opt2.of(constructor)
                    .map { it.newInstance(*arrayAny) } // spread operator
                    .ifPresent { listT += it as T }
                    .ifMissingThrow { Exception("Unable to instantiate ${t::class}") }

        } while (true == results?.next())
        return listT
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