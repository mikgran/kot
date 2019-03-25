package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MapperTest {

    data class Person(val firstName: String = "", val lastName: String = "") : DBO()

    @Test
    fun testMapping() {

        val person = Person("firstName", "lastName")

        val mapper = Mapper()

        val ormMetadata = mapper.buildOrmMetadata(person)

        assertNotNull(ormMetadata)
        assertEquals(2, ormMetadata.fieldCount, "fieldCount should be 2")

    }



}

