package mg.util.db

import mg.util.db.TestDataClasses.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class FieldCacheTest {

    @Test
    fun testGettingFields() {

        val person = FCPerson("full name", FCAddress("SomeAddress"), mutableListOf(FCBill(10, "${Date()}")))

        // this fails on parallel tests
        assertEquals(0, FieldCache.cache.size)

        val fields = FieldCache.fieldsFor(person)

        assertTrue(fields.primitives.any { it.name == "fullName" })
        assertTrue(fields.customs.any { it.name == "address" })
        assertTrue(fields.listsOfCustoms.any { it.name == "bills" })
        assertEquals(1, fields.primitives.size)
        assertEquals(1, fields.customs.size)
        assertEquals(1, fields.listsOfCustoms.size)
        assertTrue(FieldCache.cache.containsKey(person))
        // this fails on parallel tests
        assertEquals(1, FieldCache.cache.size)
    }
}

