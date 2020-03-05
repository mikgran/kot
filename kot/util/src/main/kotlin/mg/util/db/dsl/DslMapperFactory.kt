package mg.util.db.dsl

// TODO: 1 implement both DslMappers to provide SqlMysqlImpl and SqlOracleImpl
object DslMapperFactory {

    fun get(name: String? = "mysql"): DslMapper {
        return when (name) {
            "mysql",
            "mariadb" -> MySqlDslMapper()
            "oracle" -> OracleDslMapper()
            else -> throw Exception("No such implementation: $name")
        }
    }
}
