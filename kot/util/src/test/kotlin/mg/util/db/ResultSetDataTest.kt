package mg.util.db

import mg.util.common.TestUtil
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.db.functional.data.DataCell
import mg.util.db.functional.data.DataRow
import mg.util.db.functional.data.ResultSetData
import mg.util.functional.toOpt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.Connection
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

        val resultSet =
                connection.toOpt()
                        .map(Connection::createStatement)
                        .map { it.executeQuery("SELECT * FROM ${UidBuilder.buildUniqueId(rsdTest)}") }
                        .value()

        val candidate = ResultSetData.from(resultSet)

        val expectedData = ResultSetData.empty()
        val expectedColumnNames = listOf("id", "str")
        val cellTableName = UidBuilder.buildUniqueId(rsdTest).lowercase()
        val cellR1C1 = DataCell("1", "MEDIUMINT", "id", false, cellTableName)
        val cellR1C2 = DataCell("stringValue", "VARCHAR", "str", false, cellTableName)
        val cellR2C1 = DataCell("2", "MEDIUMINT", "id", false, cellTableName)
        val cellR2C2 = DataCell("somethingElseValue", "VARCHAR", "str", false, cellTableName)
        expectedData.contents() += DataRow(listOf(cellR1C1, cellR1C2), expectedColumnNames)
        expectedData.contents() += DataRow(listOf(cellR2C1, cellR2C2), expectedColumnNames)

        TestUtil.expect(expectedData.toString(), candidate.toString())

        val expectedCells = LinkedList(listOf(cellR1C1, cellR1C2, cellR2C1, cellR2C2))
        val candidateCells = LinkedList<DataCell>()
        candidate.forEach {
            it.forEach(candidateCells::add)
        }

        TestUtil.expect(expectedCells.toString(), candidateCells.toString())
    }

    @Suppress("unused")
    companion object {
        private val cleaner = TableDrop()

        @AfterAll
        @JvmStatic
        internal fun afterAll() = cleaner.dropAll()
    }
}