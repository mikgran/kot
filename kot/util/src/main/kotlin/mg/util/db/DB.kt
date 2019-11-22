package mg.util.db

import mg.util.db.dsl.BuildingBlock
import mg.util.functional.Opt2
import java.sql.Connection

// Very crude type T persistence solution
class DB {

    private var dbConfig: DBConfig = DBConfig(Config())

    fun <T : Any> save(type: T) {
        getDBO().apply {
            val connection = getConnection()
            ensureTable(type, connection)
            save(type, connection)
        }
    }

    private fun getConnection(): Connection {
        return Opt2.of(dbConfig)
                .map(DBConfig::connection)
                .filter { !it.isClosed }
                .getOrElseThrow { Exception("Connection closed.") }!!
    }

    private fun getDBO() = DBO(SqlMapperFactory.get(dbConfig.mapper ?: "mysql"))

    fun <T : Any> find(type: T): List<T> = getDBO().find(type, getConnection())

    fun <T : Any> buildUniqueId(t: T): String = getDBO().buildUniqueId(t)

    fun findByDsl(block: BuildingBlock): List<Any> = getDBO().findBy(block, getConnection())
}
