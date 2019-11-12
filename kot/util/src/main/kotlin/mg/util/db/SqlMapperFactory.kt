package mg.util.db

object SqlMapperFactory {

    fun get(type : String?) : SqlMapper = when (type) {
        "mysql" -> MySqlMapper
        else -> MySqlMapper
    }
}