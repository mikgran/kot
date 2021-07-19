package mg.util.db.functional

import mg.util.db.DBO
import mg.util.db.TableDrop
import mg.util.db.TestDataClasses.RSIPerson
import mg.util.db.TestDataClasses.RSIPerson2
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.mapIfNot
import mg.util.functional.toOpt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

internal class ResultSetIteratorTest {

    private val dbConfig = DBConfig(TestConfig())
    private val dbo = DBO(DefaultDslMapper("mysql"))
    private val connection = dbConfig.connection

    @Test
    fun testIteratingResultSet() {

        val person1 = cleaner.register(RSIPerson("test1", "test11"))
        val person2 = cleaner.register(RSIPerson("test2", "test33"))

        with(dbo) {
            ensureTable(person1, connection)
            val personsList = find(person1, connection)
            if (!personsList.containsAll(listOf(person1, person2))) {
                save(person1, connection)
                save(person2, connection)
            }
        }

        val tableUid = buildUniqueId(RSIPerson())

        val candidate = getResultSetIterator(tableUid, connection)
                .xmap { map { RSIPerson(it.getString(2), it.getString(3)) } }
                .get()!!

        assertTrue(candidate.isNotEmpty())
        assertTrue(candidate.containsAll(listOf(person1, person2)), "xmap: person1 and person2 should be in list of candidates")

        val candidate2 = getResultSetIterator(tableUid, connection)
                .lmap { rs: ResultSet -> RSIPerson(rs.getString(2), rs.getString(3)) }
                .get()!!

        assertTrue(candidate2.isNotEmpty())
        assertTrue(candidate2.containsAll(listOf(person1, person2)), "lmap: person1 and person2 should be in list of candidates")
    }

    private fun getResultSetIterator(tableUid: String, connection: Connection): Opt2<ResultSetIterator> {
        return of(connection)
                .map(Connection::createStatement)
                .mapWith(tableUid) { stmt, uid -> stmt.executeQuery("SELECT * FROM $uid") }
                .map(::iof)
    }

    @Test
    fun testPrint() {

        val person = cleaner.register(RSIPerson2("AAAAAAAAAAAA", "BBBB"))

        dbo.toOpt().x {

            ensureTable(person, connection)
            find(person, connection).contains(person).mapIfNot {
                save(person, connection)
            }

            val uid = buildUniqueId(person)
            val resultSet = connection.toOpt()
                    .map(Connection::createStatement)
                    .mapWith(uid) { stmt, tableUid -> stmt.executeQuery("SELECT * FROM $tableUid") }
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
        val cleaner = TableDrop()

        @AfterAll
        @JvmStatic
        internal fun afterAll() = cleaner.dropAll()
    }
}
