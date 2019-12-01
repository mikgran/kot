package mg.util.db

import mg.util.db.dsl.oracle.Sql as OracleSql
import mg.util.db.dsl.mysql.Sql as MySql

object SqlMapperFactory {

    fun get(type: String?): SqlMapper = when (type) {
        "mysql" -> SqlMapper(MySql())
        "oracle" -> SqlMapper(OracleSql())
        else -> SqlMapper(MySql())
    }
}