package mg.util.db

object SqlMapperFactory {
    fun getDefault(): SqlMapper = MySQLMapper
    fun getMySQLMapper() :SqlMapper = MySQLMapper
}