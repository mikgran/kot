package mg.util.db.dsl

// TODO: 9 move this functionality to Sql / SqlImpl class level.
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
