package mg.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CommonTest {

    @Test
    fun test_hasContentWithString() {

        val hasContent = Common.hasContent("string")
        assertTrue(hasContent)

        val hasContent2 = Common.hasContent("")
        assertFalse(hasContent2)

        val hasContent3 = Common.hasContent(null)
        assertFalse(hasContent3)
    }

}