package mg.util.db

import mg.util.db.dsl.BuildingBlock
import mg.util.functional.Opt2
import java.sql.Connection

// Very crude type T persistence solution, allows:
// 1. basic hard bolted methods like find, save
// 2. use of hard typed dsl free hand in case a more difficult sql is required
class DB {

    private var dbConfig: DBConfig = DBConfig(Config())
    private val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper ?: "mysql"))

    private fun getConnection(): Connection {
        return Opt2.of(dbConfig)
                .map(DBConfig::connection)
                .filter { !it.isClosed }
                .getOrElseThrow { Exception("Connection closed.") }!!
    }

    fun <T : Any> save(type: T) {
        dbo.apply {
            val connection = getConnection()
            ensureTable(type, connection)
            save(type, connection)
        }
    }

    fun <T : Any> find(type: T): List<T> = dbo.find(type, getConnection())
    fun find(block: BuildingBlock): List<Any> = dbo.findBy(block, getConnection())
}
