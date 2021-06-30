package mg.util.db.dsl

import mg.util.db.FieldCache
import mg.util.db.FieldCache.Fields
import mg.util.db.dsl.FieldAccessor.Companion.isList
import mg.util.functional.toOpt
import org.junit.jupiter.api.Assertions.*
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

    @Test
    fun testIsList() {

        data class TestType(
                val var1: Any = 1,
                val var2: List<Any> = listOf(1, 2, 3, 4),
        )

        val fields = FieldCache.fieldsFor(TestType())

        val var1 = fields.all.find { it.name == "var1" }.toOpt()
        val var2 = fields.all.find { it.name == "var2" }.toOpt()

        var2.map(::isList)
                .get()
                .apply {
                    assertTrue(this!!)
                }

        var1.map(::isList)
                .get()
                .apply {
                    assertFalse(this!!)
                }
    }

    companion object {
        const val VVVV = "VVVV"
        const val FIRST = "first"
        const val LAST = "last"
    }
}
