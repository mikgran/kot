package mg.util.db

import mg.util.common.TestUtil
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.toOpt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

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

        val candidate = ResultSetData.from(resultSet)
        
        val expectedData = ResultSetData.empty()
        val expectedRows = expectedData.contents()
        val expectedColumnNames = listOf("id", "str")
        expectedRows += ResultSetDataRow(listOf(ResultSetDataCell("1", "Integer", "id"), ResultSetDataCell("stringValue", "String", "str")), expectedColumnNames)
        expectedRows += ResultSetDataRow(listOf(ResultSetDataCell("2", "Integer", "id"), ResultSetDataCell("somethingElseValue", "String", "str")), expectedColumnNames)

        TestUtil.expect(expectedData.toString(), candidate.toString())
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