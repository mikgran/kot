package mg.util.functional

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BiOpt2Test {

    // TOIMPROVE: test coverage
    @Test
    fun test_match() {

        val biOpt = BiOpt2(Opt2.of("a"), Opt2.empty<String>())

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

        val biOpt = BiOpt2.of("a", "aa")

        assertNotNull(biOpt)
        assertNotNull(biOpt.left())
        assertNotNull(biOpt.right())
        assertEquals(Opt2.of("a"), biOpt.left())
        assertEquals(Opt2.of("aa"), biOpt.right())

        val candidate = biOpt.matchRight("", { s -> "aa" == s }, { s -> s + s })

        assertNotNull(candidate)
        assertNotNull(candidate.left())
        assertNotNull(candidate.right())
        assertEquals(Opt2.of("aa"), candidate.left()) // new left is the old right.
        assertEquals(Opt2.of("aaaa"), candidate.right())
    }

    @Test
    fun test_of() {

        val opt = Opt2.of("a")
        val biOpt = BiOpt2.of(opt, Opt2.empty<String>())

        assertNotNull(opt)
        assertNotNull(biOpt)
        assertNotNull(opt.get())
        assertNotNull(biOpt.left())
        assertNotNull(biOpt.right())
        assertEquals("a", opt.get())
    }

    @Test
    fun test_filter() {

        val biOpt = BiOpt2.of("a", null)

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

    @Test
    fun test_getLeftOrElseThrow() {

        assertDoesNotThrow {

            val biOpt = BiOpt2.of("a", "aa")
            val leftCandidate = biOpt.getLeftOrElseThrow { Exception() }

            assertNotNull(leftCandidate)
            assertEquals("a", leftCandidate)
        }

        assertThrows(Exception::class.java) {

            val biOpt2 = BiOpt2.of<String, String>(null, "")

            biOpt2.getLeftOrElseThrow { Exception() }
        }
    }

    @Test
    fun test_getRightOrElseThrow() {

        assertDoesNotThrow {

            val biOpt = BiOpt2.of("a", "aa")
            val rightCandidate = biOpt.getRightOrElseThrow { Exception() }

            assertNotNull(rightCandidate)
            assertEquals("aa", rightCandidate)
        }

        assertThrows(Exception::class.java) {

            val biOpt = BiOpt2.of<String, String>("", null)

            biOpt.getRightOrElseThrow { Exception() }
        }
    }

    @Test
    fun testRightElseLeft() {

        val biOpt = BiOpt2.of<String, String>("a", null)
                .apply {
                    assertNotNull(left())
                    assertNotNull(right())
                    assertNotNull(left().get())
                    assertNull(right().get())
                }

        biOpt.resultElseOrig()
                .also {
                    assertEquals("a", it.get())
                }

        val biOpt2 = BiOpt2.of("a", "b")
                .apply {
                    assertNotNull(left())
                    assertNotNull(right())
                    assertEquals("a", left().get())
                    assertEquals("b", right().get())
                }

        biOpt2.rightElseLeft()
                .also {
                    assertNotNull(it)
                    assertEquals("b", it.get())
                }
    }

    @Test
    fun testToRightToLeft() {

        BiOpt2.of<String, String>("a", null)
                .toRight()
                .apply {
                    assertNotNull(left())
                    assertNotNull(right())
                    assertEquals("a", left().get())
                    assertEquals("a", right().get())
                }

        BiOpt2.of("a", "b")
                .toLeft()
                .apply {
                    assertNotNull(left())
                    assertNotNull(right())
                    assertEquals("b", left().get())
                    assertEquals("b", right().get())
                }
    }

    @Test
    fun testCaseDefault() {

        BiOpt2.of<String, String>("a", null)
                .case({ it == "a" }, { it + "2" })
                .caseDefault { it }
                .also {
                    assertNotNull(it)
                    assertNotNull(it.left())
                    assertNotNull(it.right())
                    assertEquals("a", it.left().get())
                    assertEquals("a2", it.right().get())
                }

        BiOpt2.of<String, String>("a", null)
                .case({ it == "c" }, { it + "2" })
                .caseDefault { it + "3" }
                .also {
                    assertNotNull(it)
                    assertNotNull(it.left())
                    assertNotNull(it.right())
                    assertEquals("a", it.left().get())
                    assertEquals("a3", it.right().get())
                }

        BiOpt2.of<String, String>(null, null)
                .case({ it == "a" }, { "${it}2" })
                .caseDefault { "b" } // can't map nothing
                .also {
                    assertNotNull(it)
                    assertNotNull(it.left())
                    assertNotNull(it.right())
                    assertNull(it.left().get())
                    assertNull(it.right().get())
                }
    }

}
