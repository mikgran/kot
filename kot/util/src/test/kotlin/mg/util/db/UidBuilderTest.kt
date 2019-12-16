package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UidBuilderTest {

    @Test
    fun testBuild() {

        data class TestClass(val someName: String, val someInt: Int)

        val candidate = UidBuilder.buildUniqueId(TestClass("someName", 1))

        assertNotNull(candidate)
        assertEquals("TestClass${"someIntsomeName".hashCode() and 0x7fffffff}", candidate)
    }

}