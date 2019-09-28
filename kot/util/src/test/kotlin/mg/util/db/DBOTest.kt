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
    private val first1 = "first1"
    private val lastName = "lastName"
    private val last2 = "last2"

    private val testPerson = Person(firstName, lastName)
    private val testPerson2 = Person(first1, last2)

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

        val uidCandidate = dbo.buildUniqueId(Person(firstName, lastName))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastName).hashCode()}", uidCandidate)
    }

    // TOIMPROVE: test coverage
    @Test
    fun testSaveAndFindAndMap() {

        testSave()

        testMap()

        testFind()
    }


    private fun testMap() {

        // pass a constructor of the object

        val persons: ResultSet? = Opt2.of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { s -> s.executeQuery("SELECT * FROM ${dbo.buildMetadata(testPerson2).uid}") }
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("Test failed: no test data found") }
        
        val candidateMapped = dbo.map(Person(), persons)

        assertNotNull(candidateMapped)
        assertEquals("first1", candidateMapped.firstName)
        assertEquals("last2", candidateMapped.lastName)
    }

    private fun testFind(): List<Person> {
        val test1 = "test1"
        val test2 = "test2"

        val person = Person(test1, test2)

        dbo.save(person, dbConfig.connection)

        val personTest2 = Person(test1, test2)

        val candidateList = dbo.find(personTest2, dbConfig.connection)

        assertNotNull(candidateList)
        assertEquals(test1, candidateList[0].firstName)
        assertEquals(test2, candidateList[0].lastName)

        return candidateList
    }

    private fun testSave() {

        dbo.ensureTable(testPerson2, dbConfig.connection)

        dbo.save(testPerson2, dbConfig.connection)

        val candidate = Opt2.of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { s -> s.executeQuery("SELECT * FROM ${dbo.buildMetadata(testPerson2).uid}") }
                .filter(ResultSet::next)
                .map { rs -> "${rs.getString("firstName")} ${rs.getString("lastName")}" }
                .getOrElseThrow { Exception("Test failed: no test data found") }

        assertNotNull(candidate)
        assertEquals("$first1 $last2", candidate)
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

