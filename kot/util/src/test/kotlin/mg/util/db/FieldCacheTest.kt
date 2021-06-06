package mg.util.db

import mg.util.db.TestDataClasses.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class FieldCacheTest {

    @Test
    fun testGettingFields() {

        val person = FCPerson("full name", FCAddress("SomeAddress"), mutableListOf(FCBill(10, "${Date()}")))

        val fields = FieldCache.fieldsFor(person)

        assertTrue(fields.primitives.any { it.name == "fullName" })
        assertTrue(fields.customs.any { it.name == "address" })
        assertTrue(fields.listsOFCustoms.any { it.name == "bills" })
    }
}

