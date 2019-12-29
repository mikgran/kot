package mg.util.db.dsl

object DslMapperFactory {

    fun mapperFor(name: String? = "mysql"): DslMapper {
        return when (name) {
            "mysql" -> MySqlDslMapper()
            "oracle" -> OracleDslMapper()
            else -> throw Exception("No such implementation")
        }

    }

}
