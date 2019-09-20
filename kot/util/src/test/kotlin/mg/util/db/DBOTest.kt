package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DBOTest {

    private var dbConfig: DBConfig = DBConfig(TestConfig())

    data class Person(val firstName: String = "", val lastName: String = "")

    private val firstName = "firstName"
    private val lastName = "lastName"

    private val testPerson = Person(firstName, lastName)

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

    @Test
    fun testCreate() {

        val person = Person("first1", "last2")

        dbo.ensureTable(person, dbConfig.connection)

        dbo.save(person, dbConfig.connection)
    }


}

