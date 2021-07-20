package mg.util.db.functional

import mg.util.db.DBO
import mg.util.db.TableDrop
import mg.util.db.TestDataClasses.RSIPerson2
import mg.util.db.UidBuilder
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.mapIfNot
import mg.util.functional.toOpt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class PrintDataTest {

    private val dbConfig = DBConfig(TestConfig())
    private val dbo = DBO(DefaultDslMapper("mysql"))
    private val connection = dbConfig.connection

    @Test
    fun testPrint() {

        val person = cleaner.register(RSIPerson2("AAAAAAAAAAAA", "BBBB"))

        dbo.toOpt().x {

            ensureTable(person, connection)
            find(person, connection).contains(person).mapIfNot {
                save(person, connection)
            }

            val resultSet = connection.toOpt()
                    .map(Connection::createStatement)
                    .mapWith(UidBuilder.buildUniqueId(person)) { stmt, personUid ->
                        stmt.executeQuery("SELECT * FROM $personUid")
                    }
                    .get()!!

            val candidate: List<List<String>> = resultSet.getPrintData().prettyFormat()

            val expected = listOf(
                    listOf("id", "firstName   ", "lastName"),
                    listOf("1 ", "AAAAAAAAAAAA", "BBBB    ")
            )

            assertEquals(expected, candidate)
        }
    }

    @Suppress("unused")
    companion object {
        private val cleaner = TableDrop()

        @AfterAll
        @JvmStatic
        internal fun afterAll() = cleaner.dropAll()
    }

}