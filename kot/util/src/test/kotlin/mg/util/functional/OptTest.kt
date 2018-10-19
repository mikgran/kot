package mg.util.functional

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OptTest {

    @Test
    fun test_of() {

        val opt = Opt.of("a")

        assertNotNull(opt)
        assertNotNull(opt.get())
        assertEquals("a", opt.get())
    }

    @Test
    fun test_ofWithNull() {

        // TOIMPROVE: is there a way in kotlin to pass null parameter?
        val nullStr: String? = null
        val opt = Opt.of(nullStr)

        assertNotNull(opt)
        assertNull(opt.get())
    }

    @Test
    fun test_ofOpt() {

        val anotherOpt = Opt.of(VALUE)
        val opt = Opt.of(anotherOpt)

        assertNotNull(opt)
        assertEquals("value", opt.get())
    }

    @Test
    fun test_get() {

        val opt = Opt.of(VALUE)

        assertNotNull(opt)
        assertEquals("value", opt.get())

        val nullStr: String? = null
        val opt2 = Opt.of(nullStr)

        assertNotNull(opt2)
        assertNull(opt2.get())
    }

    @Test
    fun test_map() {

        val opt = Opt.of(VALUE)
        val get = opt
                .map { s -> "$s!" }
                .get()

        assertNotNull(get)
        assertEquals("$VALUE!", get)
    }

    @Test
    fun test_ifEmpty() {

        val opt = Opt.of(VALUE)

        opt.ifEmpty { "newString" }

        assertNotNull(opt)
        assertNotNull(opt.get())
        assertEquals(VALUE, opt.get())

        val nullStr: String? = null
        val opt2 = Opt.of(nullStr)

        assertNotNull(opt2)
        assertNull(opt2.get())

        val opt3 = opt2.ifEmpty { NEW_STRING }

        assertNotNull(opt3)
        assertNotNull(opt3.get())
        assertEquals(NEW_STRING, opt3.get())

        val str2 = opt2
                .ifEmpty { ANOTHER_STRING }
                .get()

        assertNotNull(str2)
        assertEquals(ANOTHER_STRING, str2)
    }

    @Test
    fun test_filter() {

        val candidate = Opt
                .of(VALUE)
                .filter { it == ANOTHER_STRING }
                .get()

        assertNull(candidate)

        val candidate2 = Opt
                .of(VALUE)
                .filter { it == VALUE }
                .get()

        assertNotNull(candidate2)
        assertEquals(VALUE, candidate2)
    }

    @Test
    fun test_equals() {
        val opt1 = Opt.of(VALUE1)
        val opt2 = Opt.of(VALUE2)

        assertNotNull(opt1)
        assertNotNull(opt2)
        assertFalse(opt1 == opt2)

        val opt3 = Opt.of(VALUE2)

        assertNotNull(opt3)
        assertTrue(opt2 == opt3)
    }

    @Test
    fun test_hashCode() {
        // collisions aside, testing with 99% deterministic way
        val opt1 = Opt.of(VALUE1)
        val opt2 = Opt.of(VALUE2)
        val opt3 = Opt.of(VALUE3)

        assertNotNull(opt1)
        assertNotNull(opt2)
        assertNotEquals(opt1.hashCode(), opt2.hashCode())

        assertNotNull(opt3)
        assertEquals(opt2.hashCode(), opt3.hashCode())
    }

    @Test
    fun test_match() {

        val opt = Opt.of(VALUE1)

        val rr = opt.match("",
                { _ -> true },
                { _ -> 3 })
                .right()

        assertNotNull(rr)
        assertEquals(3, rr.get())
    }

    @Test
    fun test_kotlin_match() {

        val anyValue = VALUE1 as Any // really not needed, but simulating incoming unknown type.

        val candidate = when {
            anyValue is String && "value1" == anyValue -> 3
            else -> 0
        }

        assertNotNull(candidate)
        assertEquals(3, candidate)

        val candidate2 = when (anyValue) {
            "value1" -> 3
            5 -> 6
            else -> 0
        }

        assertNotNull(candidate2)
        assertEquals(3, candidate2)

        val anyValue2 = 5 as Any
        val candidate3 = when (anyValue2) {
            "value1" -> 3
            5 -> 6
            else -> 0
        }

        assertNotNull(candidate3)
        assertEquals(6, candidate3)
    }

    @Test
    fun test_matchWithNullAndNoValue() {

        val stringNull: String? = null
        val opt = Opt.of(stringNull)

        val matched: BiOpt<String, Int> = opt.match("", { _ -> true }, { _ -> 3 })

        val empty = Opt.empty<String>()

        assertNotNull(matched)
        assertNull(matched.right().get())
        assertEquals(empty.get(), matched.right().get())
    }

    @Test
    fun test_matchStringValueAgainstInt() {

        val value = Opt.of(VALUE)

        val candidate = value.match(3,
                { _ -> true },
                { s -> s as Int + 1 })

        val empty = Opt.empty<Int>()

        assertNotNull(candidate)
        assertEquals(empty, candidate.left())
    }

    @Test
    fun test_matchStringValueAgainstString() {

        val value = Opt.of(VALUE)

        val candidate: BiOpt<String, String> = value.match("",
                { s -> s == "value" },
                { s -> "$s!" })

        val expected = Opt.of("$VALUE!")

        println(candidate.right().get())


        assertNotNull(candidate)
        assertEquals(expected.get(), candidate.right().get())
    }

    // ifPresent consumer & producer
    @Test
    fun test_ifPresentConsumer() {

        val value = Opt.of(VALUE)

        val candidate = TempValue("")

        value.ifPresent { s -> candidate.a = s }

        assertNotNull(candidate.a)
        assertEquals(VALUE, candidate.a)
    }

    // Note: kotlin return type definition eliminates the need for Opt.flatMap similar to in
    // java mg.util.functional.option.Opt.flatMap

    @Test
    fun test_getOrElse() {

        val opt = Opt.of(VALUE)
        val candidate = opt.getOrElse(ANOTHER_STRING)

        assertNotNull(candidate)
        assertEquals(VALUE, candidate)

        val opt2 = Opt.of<String>(null)
        val candidate2 = opt2.getOrElse(ANOTHER_STRING)

        assertNotNull(candidate2)
        assertEquals(ANOTHER_STRING, candidate2)
    }

    @Test
    fun tes_getAndMap() {

        val candidate = Opt.of(VALUE)
                .getAndMap { s -> s + "B" }

        assertNotNull(candidate)
        assertEquals(VALUE + "B", candidate)

        val candidate2 = Opt.of<String>(null)
                .getAndMap { s -> s + "X" }

        assertNull(candidate2) // no mapping if value == null
    }

    class TempValue(var a: String?)

    companion object {
        const val ANOTHER_STRING = "anotherString"
        const val NEW_STRING = "newString"
        const val VALUE = "value"
        const val VALUE1 = "value1"
        const val VALUE2 = "value2"
        const val VALUE3 = "value2"
    }
}