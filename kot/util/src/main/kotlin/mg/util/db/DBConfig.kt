package mg.util.db

import org.apache.commons.dbcp.BasicDataSource
import java.sql.Connection
import java.util.*
import java.util.Objects.requireNonNull
import javax.sql.DataSource

class DBConfig(config: Config) {

    private var properties = Properties()
    private var dbDriver: String? = null
    private var dbUrl: String? = null
    private var userName: String? = null
    private var password: String? = null

    @get:Synchronized
    @set:Synchronized
    private var dataSource: DataSource? = null
        get() {
            return when (field) {
                null -> { field = getDatasource(); field }
                else -> field
            }
        }

    private fun getDatasource(): DataSource {
        val basicDataSource = BasicDataSource()
        basicDataSource.driverClassName = dbDriver
        basicDataSource.url = dbUrl
        basicDataSource.username = userName
        basicDataSource.password = password
        return basicDataSource
    }

    val connection: Connection
        get() = dataSource?.connection ?: throw IllegalArgumentException("Datasource not defined.")

    init {
        requireNonNull(config, canNotBeNull("config"))
        properties = config.loadProperties()

        dbDriver = requireNonNull(properties.getProperty(DB_DRIVER), DB_DRIVER + NOT_DEFINED_IN_PROPERTIES)
        dbUrl = requireNonNull(properties.getProperty(DB_URL), DB_URL + NOT_DEFINED_IN_PROPERTIES)
        userName = requireNonNull(properties.getProperty(USER_NAME), USER_NAME + NOT_DEFINED_IN_PROPERTIES)
        password = requireNonNull(properties.getProperty(PASSWORD), PASSWORD + NOT_DEFINED_IN_PROPERTIES)
    }

    private fun canNotBeNull(str: String) = "$str can not be null"

    companion object {

        const val NOT_DEFINED_IN_PROPERTIES = " not defined in properties."
        const val USER_NAME = "userName"
        const val PASSWORD = "password"
        const val DB_URL = "dbUrl"
        const val DB_DRIVER = "dbDriver"
    }
}
