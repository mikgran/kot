package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MapperTest {

    data class Person(val firstName: String = "", val lastName: String = "")

    @Test
    fun testBuildingMetadata() {

        val person =  Person("firstName", "lastName")

        val mapper = Mapper()

        val metadata = mapper.buildMetadata(person)

        assertNotNull(metadata)
        assertEquals("Person", metadata.name)
        assertTrue(metadata.uid.isNotEmpty())
    }

    @Test
    fun testBuildingUid() {

        val firstName = "firstName"
        val lastname = "lastName"

        val mapper = Mapper()

        val uidCandidate = mapper.buildUniqueId(Person(firstName, lastname))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastname).hashCode()}", uidCandidate)
    }

}

