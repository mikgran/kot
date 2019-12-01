package mg.util.db

object SqlMapperFactory {

    fun get(type : String?) : SqlMapper = when (type) {
        "mysql" -> SqlMapper()
        else -> SqlMapper()
    }
}