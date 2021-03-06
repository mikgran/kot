package mg.util.db

import mg.util.db.config.Config
import mg.util.db.config.DBConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.Opt2.Factory.of
import java.sql.Connection

// Very crude type T persistence solution, allows:
// 1. basic hard bolted methods like find, save
// 2. use of hard typed dsl free hand in case a more difficult sql is required
class DB {

    private val dbConfig = DBConfig(Config())
    private val dbo = DBO(DefaultDslMapper(dbConfig.mapper))

    fun <T : Any> save(type: T) {
        val connection = getConnection()
        dbo.ensureTable(type, connection)
        dbo.save(type, connection)
    }

    fun <T : Any> find(type: T): List<T> = dbo.find(type, getConnection())

    private fun getConnection(): Connection {
        return of(dbConfig)
                .map(DBConfig::connection)
                .filter { !it.isClosed }
                .getOrElseThrow { Exception("Connection closed.") }!!
    }

}
