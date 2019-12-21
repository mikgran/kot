package mg.util.db

import org.apache.commons.dbcp.BasicDataSource
import java.sql.Connection
import java.util.*
import java.util.Objects.requireNonNull
import javax.sql.DataSource

class DBConfig(config: Config) {

    private var properties = Properties()
    // get() = field
    private var dbDriver: String? = null
    private var dbUrl: String? = null
    private var dbUserName: String? = null
    private var dbPassword: String? = null
    var mapper: String? = null

    init {
        // TOIMPROVE: CLEANUP: use kotlin requireNotNull / require ?
        // require(config, "config $STR_CAN_NOT_BE_NULL")
        requireNonNull(config, "config $STR_CAN_NOT_BE_NULL")
        properties = config.loadProperties()

        dbDriver = requireNonNull(properties.getProperty(DB_DRIVER), DB_DRIVER + NOT_DEFINED_IN_PROPERTIES)
        dbUrl = requireNonNull(properties.getProperty(DB_URL), DB_URL + NOT_DEFINED_IN_PROPERTIES)
        dbUserName = requireNonNull(properties.getProperty(DB_USER_NAME), DB_USER_NAME + NOT_DEFINED_IN_PROPERTIES)
        dbPassword = requireNonNull(properties.getProperty(DB_PASSWORD), DB_PASSWORD + NOT_DEFINED_IN_PROPERTIES)
        mapper = requireNonNull(properties.getProperty(MAPPER), MAPPER + NOT_DEFINED_IN_PROPERTIES)
    }

    @get:Synchronized
    @set:Synchronized
    private var dataSource: DataSource? = null
        get() {
            return when (field) {
                null -> {
                    field = getDatasource(); field
                }
                else -> field
            }
        }

    private fun getDatasource(): DataSource {
        val basicDataSource = BasicDataSource()
        with(basicDataSource) {
            driverClassName = dbDriver
            url = dbUrl
            username = dbUserName
            password = dbPassword
        }
        return basicDataSource
    }

    val connection: Connection
        get() = dataSource?.connection ?: throw IllegalArgumentException("DataSource not defined.")

    companion object {
        const val STR_CAN_NOT_BE_NULL = "can not be null"
        const val NOT_DEFINED_IN_PROPERTIES = " not defined in properties."
        const val DB_USER_NAME = "userName"
        const val DB_PASSWORD = "password"
        const val DB_URL = "dbUrl"
        const val DB_DRIVER = "dbDriver"
        const val MAPPER = "mapper"
    }
}
