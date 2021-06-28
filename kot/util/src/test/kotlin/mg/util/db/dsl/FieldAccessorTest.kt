package mg.util.db.dsl

import mg.util.db.FieldCache
import mg.util.db.FieldCache.Fields
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Field

internal class FieldAccessorTest {

    data class Person(var firstName: String = FIRST, var lastName: String = LAST)

    @Test
    fun testFieldGet() {

        val person = Person()
        val fieldsOfPerson: Fields = FieldCache.fieldsFor(person)
        val listOfCandidates = mutableListOf<Any>()

        fieldsOfPerson.primitives.forEach { field: Field ->
            listOfCandidates += FieldAccessor.fieldGet(field, person)
        }

        assertTrue(listOfCandidates.containsAll(listOf(FIRST, LAST)))
    }

    private val s: String
        get() {
            val vvvv = "VVVV"
            return vvvv
        }

    @Test
    fun testFieldSet() {

        val person = Person()
        val fieldsOfPerson = FieldCache.fieldsFor(person)

        assertEquals(FIRST, person.firstName)
        assertEquals(LAST, person.lastName)

        fieldsOfPerson.primitives.forEach { field: Field ->
            FieldAccessor.fieldSet(field, person, VVVV)
        }

        assertEquals(VVVV, person.firstName)
        assertEquals(VVVV, person.lastName)
    }


    // TOIMPROVE: Coverage


    companion object {
        const val VVVV = "VVVV"
        const val FIRST = "first"
        const val LAST = "last"
    }
}