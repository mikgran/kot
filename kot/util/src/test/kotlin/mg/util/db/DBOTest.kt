package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.isAccessible

internal class DBOTest {

    data class Person(val firstName: String = "", val lastName: String = "")

    private val firstName = "firstName"
    private val lastName = "lastName"

    private val testPerson = Person(firstName, lastName)

    @Test
    fun testBuildingMetadata() {

        val dbo = DBO()

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

        val dbo = DBO()

        val uidCandidate = dbo.buildUniqueId(Person(firstName, lastname))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastname).hashCode()}", uidCandidate)
    }

}

