package mg.util.common

import mg.util.common.Common.hasAnyContent
import mg.util.common.Common.hasContent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CommonTest {

    @Test
    fun test_hasContentWithString() {

        val hasContent = hasContent("string")
        assertTrue(hasContent)

        val hasContent2 = hasContent("")
        assertFalse(hasContent2)

        val strNull: String? = null
        val hasContent3 = hasContent(strNull)
        assertFalse(hasContent3)
    }

    @Test
    fun `test hasContent with Any`() {

        val nullObj: Any? = null
        val someObj: Int = Int.MIN_VALUE

        val candidate = hasAnyContent(nullObj)
        assertFalse(candidate, "null should yield false")

        val candidate2 = hasAnyContent(someObj)
        assertTrue(candidate2, "a non null should yield true")
    }

    @Test
    fun `test hasContent with List`() {

        val nullObj: List<Int>? = null
        val someObj: List<Int> = listOf(1, 2, 3, 4, 5)

        val candidate = hasContent(someObj)
        assertTrue(candidate, "a non null should yield true")

        val candidate2 = hasContent(nullObj)
        assertFalse(candidate2, "a null should yield false")

    }

    @Test
    fun `test plus on StringBuilder and String`() {

        val stringBuilder = StringBuilder()
        stringBuilder +
                HELLO +
                " " +
                WORLD

        assertEquals(HELLO_WORLD, stringBuilder.toString())
    }

    @Test
    fun `test plus on StringBuilder and StringBuilder`() {

        val stringBuilder1 = StringBuilder() + HELLO
        val stringBuilder2 = StringBuilder() + " $WORLD"
        val candidate = stringBuilder1 + stringBuilder2

        assertEquals(HELLO_WORLD, stringBuilder1.toString())
        assertEquals(" $WORLD", stringBuilder2.toString())
        assertEquals(HELLO_WORLD, candidate.toString())
        assertEquals(stringBuilder1, candidate)
    }

    @Test
    fun testSplitWithDelimiters() {

        val testString = "hello TO world MY dear friend"

        val candidate = Common.splitWithDelimiters(testString, listOf("TO", "MY"))

        val expected = listOf("hello ", "TO world ", "MY dear friend")

        TestUtil.expect(expected.toString(), candidate.toString())

        val candidate2 = Common.splitWithDelimiters(testString, listOf("NOTFOUND"))

        val expected2 = listOf(testString)

        TestUtil.expect(expected2, candidate2)
    }

    companion object {
        private const val HELLO_WORLD = "hello world"
        private const val HELLO = "hello"
        private const val WORLD = "world"
    }
}