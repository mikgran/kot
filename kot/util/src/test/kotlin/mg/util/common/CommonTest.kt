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
        val someObj: Int? = Int.MIN_VALUE

        val candidate = hasAnyContent(nullObj)
        assertFalse(candidate, "null should yield false")

        val candidate2 = hasAnyContent(someObj)
        assertTrue(candidate2, "a non null should yield true")
    }

    @Test
    fun `test hasContent with List`() {

        val nullObj: List<Int>? = null
        val someObj: List<Int>? = listOf(1, 2, 3, 4, 5)

        val candidate = hasContent(someObj)
        assertTrue(candidate, "a non null should yield true")

        val candidate2 = hasContent(nullObj)
        assertFalse(candidate2, "a null should yield false")

    }

    @Test
    fun `test plus on StringBuilder`() {

        val stringBuilder = StringBuilder()
        stringBuilder +
                "hello" +
                " " +
                "world"

        assertEquals("hello world", stringBuilder.toString())
    }
}