package mg.util.db

object DslMapperFactory {

    fun get(type : String?) : DslMapper = when (type) {
        "mysql" -> MySqlDslMapper
//        "microsoftsqlserver" -> MicrosoftSqlServerDslMapper
//        "oracle" -> OracleDslMapper
//        "db2" -> Db2DslMapper
//        "postgre" -> PostgreDslMapper
//        "mariadb" -> MariaDbDslMapper
//        ...
        else -> MySqlDslMapper
    }
}