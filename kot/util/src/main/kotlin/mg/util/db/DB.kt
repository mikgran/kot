package mg.util.db

import mg.util.functional.Opt2
import java.sql.Connection

// TOCONSIDER: remove wrapper?
// Very crude type T persistence solution
class DB {

    private var dbConfig: DBConfig = DBConfig(Config())

    fun <T : Any> save(type: T) {

        val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))
        val connection = getConnection()
        dbo.ensureTable(type, connection)
        dbo.save(type, connection)
    }

    private fun getConnection(): Connection {
        return Opt2.of(dbConfig)
                .map(DBConfig::connection)
                .filter { !it.isClosed }
                .getOrElseThrow { Exception("Connection closed.") }!!
    }

    fun <T : Any> find(type: T): List<T> {

        val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))
        val connection = getConnection()
        return dbo.find(type, connection)
    }

    fun <T : Any> buildUniqueId(t: T): String {
        return DBO(SqlMapperFactory.get(dbConfig.mapper ?: "mysql")).buildUniqueId(t)
    }

}