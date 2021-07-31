package mg.util.db.dsl

import mg.util.common.TestUtil
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
                .apply {
                    assertNotNull(get())
                    assertEquals(true, get())
                }

        var1.map(::isList)
                .apply {
                    assertNotNull(get())
                    assertEquals(false, get())
                }
    }

    @Test
    fun testUniqueChildrenByParentImpl() {

        data class A5(var txt5: String = "5")
        data class A4(var txt4: String = "4")
        data class A3(var txt3: String = "3", var a4: A4 = A4())
        data class A2(var txt2: String = "2", var a3list: List<A3> = listOf(A3(), A3()))
        data class A1(var txt: String = "1", var a2: A2 = A2(), var a5: A5 = A5())

        val candidate = FieldAccessor.uniqueChildrenByParent(A1())

        val expected = linkedMapOf(
                A1() to listOf(A2(), A5()),
                A2() to listOf(A3()),
                A3() to listOf(A4())
        )

        TestUtil.expect(expected, candidate)
    }

    companion object {
        const val VVVV = "VVVV"
        const val FIRST = "first"
        const val LAST = "last"
    }
}
