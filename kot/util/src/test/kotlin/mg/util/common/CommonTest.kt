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



}