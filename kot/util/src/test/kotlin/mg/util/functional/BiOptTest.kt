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
    fun test_matchRight() {

        val biOpt = BiOpt.of("a", "aa")

        assertNotNull(biOpt)
        assertNotNull(biOpt.left())
        assertNotNull(biOpt.right())
        assertEquals(Opt.of("a"), biOpt.left())
        assertEquals(Opt.of("aa"), biOpt.right())

        val candidate = biOpt.matchRight("", { s -> "aa" == s }, { s -> s + s })

        assertNotNull(candidate)
        assertNotNull(candidate.left())
        assertNotNull(candidate.right())
        assertEquals(Opt.of("aa"), candidate.left()) // new left is the old right.
        assertEquals(Opt.of("aaaa"), candidate.right())
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

    @Test
    fun test_filter() {

        val biOpt = BiOpt.of("a", null)

        val candidate = biOpt.filter { s -> s == "a" }

        assertNotNull(candidate)
        assertNotNull(candidate.left())
        assertNotNull(candidate.right())
        assertNull(candidate.right().get())
        assertEquals("a", candidate.left().get())

        val candidate2 = biOpt.filter { it == "b" }

        assertNotNull(candidate2)
        assertNotNull(candidate2.left())
        assertNotNull(candidate2.right())
        assertNull(candidate2.left().get())
        assertNull(candidate2.right().get())
    }
}