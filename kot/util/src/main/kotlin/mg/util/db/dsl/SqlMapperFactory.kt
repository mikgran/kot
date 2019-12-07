package mg.util.db.dsl

import mg.util.db.dsl.SqlMapper
import mg.util.db.dsl.oracle.Sql as OracleSql
import mg.util.db.dsl.mysql.Sql as MySql

object SqlMapperFactory {

    fun get(type: String?): SqlMapper = when (type) {
        "mysql" -> SqlMapper(MySql())
        "oracle" -> SqlMapper(OracleSql())
        else -> SqlMapper(MySql())
    }
}