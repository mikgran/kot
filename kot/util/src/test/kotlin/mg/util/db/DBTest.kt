package mg.util.db

import mg.util.common.Common.hasContent
import mg.util.common.Common.nonThrowingBlock
import mg.util.functional.BiOpt
import mg.util.functional.BiOpt2
import mg.util.functional.Opt2
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

internal class DBTest {

    data class PersonB(val firstName: String = "", val lastName: String = "")

    private var dbConfig: DBConfig = DBConfig(Config())
    private val fName = "firstName"
    private val lName = "lastName"

    @Test
    fun testSaving() {
        assertDoesNotThrow {

            val connection = dbConfig.connection

            val db = DB()
            val testPerson = PersonB(fName, lName)
            val uniqueId = db.buildUniqueId(PersonB("", ""))

            db.save(testPerson)

            val candidate = Opt2.of(connection)
                    .map(Connection::createStatement)
                    .map { statement -> statement.executeQuery("SELECT * FROM $uniqueId") }
                    .filter(ResultSet::next)
                    .map { resultSet -> resultSet.getString(fName) + " " + resultSet.getString(lName) }
                    .getOrElse("")

            assertEquals("$fName $lName", candidate)
        }
    }

    @Test
    internal fun testDBODelegate() {

        val db = DB()
        val uid = db.buildUniqueId(PersonB("", ""))
        assertNotNull(uid)
        assertTrue(hasContent(uid))
    }

    companion object {

        @AfterAll
        @JvmStatic
        internal fun afterTest() {

            val person = PersonB("first1", "last2")
            val dbConfig = DBConfig(Config())
            val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))
            val uidTableName = dbo.buildMetadata(person).uid
            val sqlCommands = listOf("DELETE FROM $uidTableName", "DROP TABLE $uidTableName")

            val bi2  = Opt2.of(dbConfig.connection)
                    .map(Connection::createStatement)
                    .map { statement ->

                        sqlCommands.forEach { sqlCommand ->
                            nonThrowingBlock { statement.executeUpdate(sqlCommand) }
                        }
                    }
        }
    }
}