package mg.util.functional

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class OptTest {

    @Test
    fun test_of() {

        val opt = Opt.of("a")

        assertNotNull(opt)
        assertEquals("a", opt.get())
    }

    @Test
    fun test_ofWithNull() {

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
}