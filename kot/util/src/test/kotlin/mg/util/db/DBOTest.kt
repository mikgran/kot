package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DBOTest {

    data class Person(val firstName: String = "", val lastName: String = "")

    private val firstName = "firstName"
    private val lastName = "lastName"

    private fun testPerson() = Person(firstName, lastName)

    @Test
    fun testBuildingMetadata() {

        val person = testPerson()

        val mapper = DBO()

        val metadata = mapper.buildMetadata(person)

        assertNotNull(metadata)
        assertEquals("Person", metadata.name)
        assertTrue(metadata.uid.isNotEmpty())
    }

//    @Test
//    fun testBuildingMetadataToSql() {
//
//        val person = testPerson()
//
//        val mapper = DBO()
//
//        val metadata = mapper.buildMetadata(person)
//
//        assertNotNull(metadata)
//        assertEquals("", "")
//
//    }

    @Test
    fun testBuildingUid() {

        val firstName = "firstName"
        val lastname = "lastName"

        val mapper = DBO()

        val uidCandidate = mapper.buildUniqueId(Person(firstName, lastname))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastname).hashCode()}", uidCandidate)
    }

}

