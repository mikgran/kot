package mg.util.db

import mg.util.functional.Opt2
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

internal class DBOTest {

    private var dbConfig: DBConfig = DBConfig(TestConfig())

    data class Person(val firstName: String = "", val lastName: String = "")

    private val firstName = "firstName"
    private val lastName = "lastName"

    private val testPerson = Person(firstName, lastName)
    private val testPerson2 = Person("first1", "last2")

    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testBuildingMetadata() {


        val metadataCandidate: Metadata<Person> = dbo.buildMetadata(testPerson)

        assertNotNull(metadataCandidate)
        assertEquals("Person", metadataCandidate.name)
        assertEquals(2, metadataCandidate.fieldCount)
        assertEquals(Person::class, metadataCandidate.type::class)
        assertNotNull(metadataCandidate.uid)
        assertTrue(metadataCandidate.uid.isNotEmpty())

    }

    @Test
    fun testBuildingUid() {

        val firstName = "firstName"
        val lastname = "lastName"

        val uidCandidate = dbo.buildUniqueId(Person(firstName, lastname))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastname).hashCode()}", uidCandidate)
    }

    // TOIMPROVE: test coverage
    @Test
    fun testSavePersonData() {

        dbo.ensureTable(testPerson2, dbConfig.connection)

        dbo.save(testPerson2, dbConfig.connection)

        val candidate = Opt2.of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { s -> s.executeQuery("SELECT * FROM ${dbo.buildMetadata(testPerson2).uid}") }
                .filter(ResultSet::next)
                .map { rs -> "${rs.getString(2)} ${rs.getString(3)}" }
                .getOrElseThrow { Exception("Test failed: no test data found") }

        assertNotNull(candidate)
        assertEquals("first1 last2", candidate)
    }


    companion object {

        @AfterAll
        @JvmStatic
        internal fun afterAll() {

            val person = Person("first1", "last2")
            val dbConfig = DBConfig(TestConfig())
            val dbo = DBO(SqlMapperFactory.get("mysql"))
            val uidTableName = dbo.buildMetadata(person).uid

            val statement = Opt2.of(dbConfig.connection)
                    .map(Connection::createStatement)

            statement.map { it.executeUpdate("DELETE FROM $uidTableName") }

            statement.map { it.executeUpdate("DROP TABLE $uidTableName") }
        }

    }

}

