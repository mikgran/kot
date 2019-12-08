package mg.util.db

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.SqlMapperFactory
import mg.util.functional.Opt2.Factory.of
import java.sql.Connection

// Very crude type T persistence solution, allows:
// 1. basic hard bolted methods like find, save
// 2. use of hard typed dsl free hand in case a more difficult sql is required
class DB {

    private val dbConfig = DBConfig.config
    private val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper ?: "mysql"))

    fun <T : Any> save(type: T) {
        val connection = getConnection()
        dbo.ensureTable(type, connection)
        dbo.save(type, connection)
    }

    fun <T : Any> find(type: T): List<T> = dbo.find(type, getConnection())

    @Suppress("unused")
    fun findBy(block: BuildingBlock): List<Any> = dbo.findBy(block, getConnection())

    private fun getConnection(): Connection {
        return of(dbConfig)
                .map(DBConfig::connection)
                .filter { !it.isClosed }
                .getOrElseThrow { Exception("Connection closed.") }!!
    }

}
