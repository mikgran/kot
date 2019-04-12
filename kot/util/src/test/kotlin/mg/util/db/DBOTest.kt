package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DBOTest {

    data class Person(val firstName: String = "", val lastName: String = "")

    private val firstName = "firstName"
    private val lastName = "lastName"

    private val testPerson = Person(firstName, lastName)

    @Test
    fun testBuildingMetadata() {

        val dbo = DBO()

        val metadata = dbo.buildMetadata(testPerson)

        assertNotNull(metadata)
        assertEquals("Person", metadata.name)
        assertTrue(metadata.uid.isNotEmpty())
    }

    @Test
    fun testBuildingUid() {

        val firstName = "firstName"
        val lastname = "lastName"

        val dbo = DBO()

        val uidCandidate = dbo.buildUniqueId(Person(firstName, lastname))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastname).hashCode()}", uidCandidate)
    }

    @Test
    fun testBuildingMetadataToSql() {

        val dbo = DBO()

        val metadata = dbo.buildMetadata(testPerson)

//        val personSqlCandidate = MySQLMapper.createTable(metadata)
//
//        assertNotNull(personSqlCandidate)
//        assertEquals("CREATE TABLE PERSONS(id MEDIUMINT NOT NULL AUTO_INCREMENT, firstname VARCHAR(64) NOT NULL, lastname VARCHAR(64) NOT NULL)", personSqlCandidate)

    }

}

