package mg.util.functional

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BiOptTest {

    @Test
    fun test_match() {

        val biOpt = BiOpt(Opt.of("a"), Opt.empty<String>())

        assertNotNull(biOpt)
        assertNotNull(biOpt.left())
        assertNotNull(biOpt.right())

        val candidate = biOpt.match("", { s -> "a" == s }, { s -> s + s })

        assertNotNull(candidate.left())
        assertNotNull(candidate.right())
        assertEquals("a", candidate.left().get())
        assertEquals("aa", candidate.right().get())
    }

    @Test
    fun test_of() {

        val opt = Opt.of("a")
        val biOpt = BiOpt.of(opt, Opt.empty<String>())

        assertNotNull(opt)
        assertNotNull(biOpt)
        assertNotNull(opt.get())
        assertNotNull(biOpt.left())
        assertNotNull(biOpt.right())
        assertEquals("a", opt.get())


    }

}