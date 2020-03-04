package mg.util.db.functional

import mg.util.db.DBO
import mg.util.db.TestDataClasses.RSIPerson
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.SqlMapper
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

internal class ResultSetIteratorTest {

    private val dbConfig = DBConfig(TestConfig())
    private val dbo = DBO(SqlMapper("mysql"))
    private val person1 = RSIPerson("test1", "test11")
    private val person2 = RSIPerson("test2", "test33")

    @Test
    fun testIteratingResultSet() {

        val connection = dbConfig.connection

        with(dbo) {
            ensureTable(person1, connection)
            val personsList = find(person1, connection)
            if (!personsList.containsAll(listOf(person1, person2))) {
                save(person1, connection)
                save(person2, connection)
            }
        }

        val tableUid = buildUniqueId(RSIPerson())

        val candidate = getResultSetIterator(connection, tableUid)
                .xmap { map { RSIPerson(it.getString(2), it.getString(3)) } }
                .get()!!

        assertTrue(candidate.isNotEmpty())
        assertTrue(candidate.containsAll(listOf(person1, person2)), "xmap: person1 and person2 should be in list of candidates")

        val candidate2 = getResultSetIterator(connection, tableUid)
                .lmap { rs: ResultSet -> RSIPerson(rs.getString(2), rs.getString(3)) }
                .get()!!

        assertTrue(candidate2.isNotEmpty())
        assertTrue(candidate2.containsAll(listOf(person1, person2)), "lmap: person1 and person2 should be in list of candidates")
    }

    private fun getResultSetIterator(connection: Connection, tableUid: String): Opt2<ResultSetIterator> {
        return of(connection)
                .map(Connection::createStatement)
                .mapWith(tableUid) { stmt, uid -> stmt.executeQuery("SELECT * FROM $uid") }
                .map(::iof)
    }

}