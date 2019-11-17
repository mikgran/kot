package mg.util.common

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import mg.util.common.Common.hasContent

internal class CommonTest {

    @Test
    fun test_hasContentWithString() {

        val hasContent = hasContent("string")
        assertTrue(hasContent)

        val hasContent2 = hasContent("")
        assertFalse(hasContent2)

        val hasContent3 = hasContent(null)
        assertFalse(hasContent3)
    }

    @Test
    fun `test hasContent with Any`() {

        val nullObj: Any? = null
        val someObj: Int? = Int.MIN_VALUE

        val candidate = hasContent(nullObj)
        assertFalse(candidate, "null should yield false")

        val candidate2 = hasContent(someObj)
        assertTrue(hasContent(candidate2), "a non null should yield true")
    }



}