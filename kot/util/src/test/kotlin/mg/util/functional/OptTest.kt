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
        assertEquals(null, opt.get())
    }

    @Test
    fun test_ofOpt() {

        val anotherOpt = Opt.of("value")
        val opt = Opt.of(anotherOpt)

        assertNotNull(opt)
        assertEquals("value", opt.get())
    }


    @Test
    fun test_get() {

        val opt = Opt.of("value")

        assertNotNull(opt)
        assertEquals("value", opt.get())

        val nullStr: String? = null
        val opt2 = Opt.of(nullStr)

        assertNotNull(opt2)
        assertNull(opt2.get())
    }

    @Test
    fun test_map() {

        val opt = Opt.of("value")
        val get = opt
                .map { s -> "$s!" }
                .get()

        assertNotNull(get)
        assertEquals("value!", get)
        // TOIMPROVE: coverage
    }

    @Test
    fun test_ifEmpty() {

        val opt = Opt.of("aString")

        opt.ifEmpty { "newString" }

        assertNotNull(opt)
        assertNotNull(opt.get())
        assertEquals("aString", opt.get())

        val nullStr: String? = null
        val opt2 = Opt.of(nullStr)

        assertNotNull(opt2)
        assertNull(opt2.get())

        val opt3 = opt2.ifEmpty { "newString" }

        assertNotNull(opt3)
        assertNotNull(opt3.get())
        assertEquals("newString", opt3.get())

        val str2 = opt2
                .ifEmpty { "anotherString" }
                .get()

        assertNotNull(str2)
        assertEquals("anotherString", str2)
    }

    @Test
    fun test_filter() {

        val candidate = Opt
                .of("aString")
                .filter { it == "anotherString" }
                .get()

        assertNull(candidate)

        val candidate2 = Opt
                .of("aString")
                .filter { it == "aString" }
                .get()

        assertNotNull(candidate2)
        assertEquals("aString", candidate2)
    }
}