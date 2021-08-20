package mg.util.db

import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.Opt2
import mg.util.functional.toOpt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

internal class ResultSetDataTest {

    private val dbConfig = DBConfig(TestConfig())
    private val dbo = DBO(DefaultDslMapper("mysql"))
    private val connection = dbConfig.connection

    @Test
    fun testCreatingRSD1() {

        data class RSDTest(var str: String = "")

        val rsdTest = RSDTest("stringValue")
        val rsdTest2 = RSDTest("somethingElseValue")
        cleaner.register(rsdTest)

        dbo.ensureTable(rsdTest, connection)
        dbo.save(rsdTest, connection)
        dbo.save(rsdTest2, connection)

        val resultSet = getResultSet(connection, "SELECT * FROM ${UidBuilder.buildUniqueId(rsdTest)}")

        val resultSetData = ResultSetData.from(resultSet)

        println(resultSetData)

        // val expected = ResultSetData.empty()



        // TestUtil.expect()
    }

    private fun getResultSet(connection: Connection, sqlString: String): ResultSet {
        return connection.toOpt()
                .map(Connection::createStatement)
                .map { it.executeQuery(sqlString) }
                .value()
    }

    @Suppress("unused")
    companion object {
        private val cleaner = TableDrop()

        @AfterAll
        @JvmStatic
        internal fun afterAll() = cleaner.dropAll()
    }
}