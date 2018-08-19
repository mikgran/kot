package mg.util.functional

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OptTest {

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
                .get()

        assertNotNull(rr)
        assertEquals(3, rr)
    }

    @Test
    fun test_matchWithNullAndNoValue() {

        val stringNull: String? = null
        val opt = Opt.of(stringNull)

        val matched = opt.match("",
                { _ -> true },
                { _ -> 3 })

        val empty = Opt.empty<String>()

        assertNotNull(matched)
        assertNull(matched.get())
        assertEquals(empty.get(), matched.get())
    }

    @Test
    fun test_matchStringValueAgainstInt() {

        val value = Opt.of(VALUE)

        val candidate = value.match(3,
                { _ -> true },
                { s -> s + 1 })

        val empty = Opt.empty<Int>()

        assertNotNull(candidate)
        assertEquals(empty.get(), candidate.get())
    }

    @Test
    fun test_matchStringValueAgainstString() {

        val value = Opt.of(VALUE)

        val candidate = value.match("",
                { s -> s == "value" },
                { s -> "$s!" })

        val expected = Opt.of("$VALUE!")

        assertNotNull(candidate)
        assertEquals(expected.get(), candidate.get())
    }

    companion object {
        const val ANOTHER_STRING = "anotherString"
        const val NEW_STRING = "newString"
        const val VALUE = "value"
        const val VALUE1 = "value1"
        const val VALUE2 = "value2"
        const val VALUE3 = "value2"
    }
}