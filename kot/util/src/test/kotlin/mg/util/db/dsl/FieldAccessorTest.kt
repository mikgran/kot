package mg.util.db.dsl

import mg.util.db.FieldCache
import mg.util.db.FieldCache.Fields
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

    // TOIMPROVE: Coverage


    companion object {
        const val FIRST = "first"
        const val LAST = "last"
    }
}