package mg.util.db

import mg.util.common.Common.nonThrowingBlock
import mg.util.db.TestDataClasses.PersonB
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.Config
import mg.util.db.config.DBConfig
import mg.util.db.dsl.SqlMapper
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class DBTest {

    private var dbConfig = DBConfig(Config())
    private val fName = "firstName"
    private val lName = "lastName"

    @Test
    fun testSaving() {
        assertDoesNotThrow {

            val connection = dbConfig.connection

            val db = DB()
            val testPerson = PersonB(fName, lName)
            val uniqueId = buildUniqueId(PersonB("", ""))

            db.save(testPerson)

            val candidates = Opt2.of(connection)
                    .map(Connection::createStatement)
                    .map { it.executeQuery("SELECT * FROM $uniqueId") }
                    .map(::iof)
                    .map { it.map { i -> PersonB(i.getString(fName), i.getString(lName)) } }
                    .getOrElse(ArrayList())

            assertTrue(candidates.isNotEmpty())
            assertTrue(candidates.any { it.firstName == "aa" && it.lastName == "bb" })
        }
    }

    @Test
    fun testFind() {

        val db = DB()
        val testPerson = PersonB("aa", "bb")

        db.save(testPerson)

        val personListCandidate = db.find(PersonB())

        assertTrue(personListCandidate.isNotEmpty())
        assertTrue(personListCandidate.any { it.firstName == "aa" && it.lastName == "bb" })
    }

    @Test
    fun testFindById() {

        val db = DB()
        val testPerson = PersonB("11", "22")
        db.save(testPerson)

        // db.findBySql { select PersonB() where it::firstName eq "name" }
        // SELECT * FROM person12345 as p WHERE p.firstName = 'name'
    }

    @Suppress("unused")
    companion object {

        @AfterAll
        @JvmStatic
        internal fun afterTest() {

            val person = PersonB("first1", "last2")
            val dbConfig = DBConfig(Config())
            val dbo = DBO(SqlMapper(dbConfig.mapper))
            val uidTableName = dbo.buildMetadata(person).uid
            val sqlCommandsList = listOf("DELETE FROM $uidTableName", "DROP TABLE $uidTableName")

            Opt2.of(dbConfig.connection)
                    .map(Connection::createStatement)
                    .mapWith(sqlCommandsList) { statement, sqlCommands ->

                        sqlCommands.forEach { sqlCommand ->
                            nonThrowingBlock { statement.executeUpdate(sqlCommand) }
                        }
                    }
        }
    }
}
