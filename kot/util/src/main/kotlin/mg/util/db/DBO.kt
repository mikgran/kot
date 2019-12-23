package mg.util.db

import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslMapper
import mg.util.db.dsl.SqlMapper
import mg.util.db.dsl.mysql.SelectBlock
import mg.util.functional.Opt2.Factory.of
import java.lang.reflect.Field
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.full.memberProperties

// a simple Object-Relational-Mapping class
class DBO(private val mapper: SqlMapper) {

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

        val listSqls = of(t)
                .map(::buildMetadata)
                .map(mapper::buildCreateTable)
                .map { it.split(";") }
                .ifMissingThrow { Exception(UNABLE_TO_BUILD_CREATE_TABLE) }
                .getOrElse { emptyList() }

        of(listSqls)
                .filter { it.isNotEmpty() }
                .case({ it.size == 1 }, { singleUpdate(connection, it) })
                .case({ it.size > 1 }, { batchUpdate(connection, it) })
    }

    private fun batchUpdate(connection: Connection, listSqls: List<String>) {
        of(getStatement(connection))
                .mapWith(listSqls) { stmt, sqls -> sqls.map { stmt.addBatch(it) }; stmt }
                .map { it.executeBatch() }
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_TABLE) }
    }

    private fun singleUpdate(connection: Connection, listSqls: List<String>) {
        of(getStatement(connection))
                .mapWith(listSqls) { stmt, sqls -> stmt.executeUpdate(sqls[0]) }
                .getOrElseThrow { Exception(UNABLE_TO_CREATE_TABLE) }
    }

    // Collection<String> kotlin classes not included
    // Collection<Any> is not included
    // Collection<Simple> custom classes are included
    private fun <T : Any> getNonKotlinFields(t: T): List<Any> {

        val customFields = t::class.java.declaredFields
                .asList()
                .map(Field::getType)
                .filter { !it.packageName.startsWith("kotlin") }

        val customFieldsOfCollections = t::class.java.declaredFields
                .asList()
                .map(Field::getType)
                .filter { it is List<*> }
                .map { it as List<*> }

        customFieldsOfCollections
                .filter(::isNotCollectionTypeKotlin)

        return customFields
    }

    private fun isNotCollectionTypeKotlin(list: List<*>): Boolean = !isCollectionTypeKotlin(list)
    private fun isCollectionTypeKotlin(list: List<*>): Boolean {
        if (list.isNotEmpty()) {
            return list[0]!!::class.java.packageName.startsWith("kotlin")
        }
        return false
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

