package mg.util.db.dsl

import mg.util.db.dsl.mysql.Sql as MySql
import mg.util.db.dsl.oracle.Sql as OracleSql

object SqlMapperFactory {

    fun get(type: String?): SqlMapper = when (type) {
        "mysql" -> SqlMapper(MySql())
        "oracle" -> SqlMapper(OracleSql())
        else -> SqlMapper(MySql())
    }
}