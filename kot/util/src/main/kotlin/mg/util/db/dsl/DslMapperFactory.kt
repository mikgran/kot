package mg.util.db.dsl

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
