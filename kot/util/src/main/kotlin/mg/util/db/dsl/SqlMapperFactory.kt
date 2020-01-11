package mg.util.db.dsl

import mg.util.db.dsl.mysql.Sql as MySql
import mg.util.db.dsl.oracle.Sql as OracleSql

object SqlMapperFactory {

    private const val MYSQL = "mysql"
    private const val ORACLE = "oracle"

    fun get(type: String?): SqlMapper = when (type) {
        MYSQL -> SqlMapper(MySql(), MYSQL)
        ORACLE -> SqlMapper(OracleSql(), ORACLE)
        else -> SqlMapper(MySql(), MYSQL)
    }
}