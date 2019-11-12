package mg.util.db

object DslMapperFactory {

    fun get(type : String?) : MySqlDslMapper.Companion = when (type) {
        "mysql" -> MySqlDslMapper
        else -> MySqlDslMapper
    }
}