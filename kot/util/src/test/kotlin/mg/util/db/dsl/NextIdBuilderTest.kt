package mg.util.db.dsl

import mg.util.common.Common.classSimpleName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class NextIdBuilderTest {

    @Test
    fun testBuildingLastId() {

        data class TestClass1(var s: String = "s", var s2: String = "s")
        data class SomeTestClass2(var s: String = "s")

        var className = "" + TestClass1().classSimpleName()
        var idCandidate: String?

        assertEquals(0, nextIdBuilderContentSize())

        idCandidate = NextIdBuilder.build(className)

        assertEquals("TestClass11", idCandidate)
        assertEquals(1, nextIdBuilderContentSize())

        idCandidate = NextIdBuilder.build(className)

        assertEquals("TestClass12", idCandidate)
        assertEquals(1, nextIdBuilderContentSize())

        val className2 = "" + SomeTestClass2().classSimpleName()
        idCandidate = NextIdBuilder.build(className2)

        assertEquals("SomeTestClass21", idCandidate)
        assertEquals(2, nextIdBuilderContentSize())

        val expectedMap = mutableMapOf(className to 2, className2 to 1)
        val contents = NextIdBuilder.contents().contents()
        assertTrue(contents.entries.containsAll(expectedMap.entries))

    }

    private fun nextIdBuilderContentSize() = NextIdBuilder.contents().contents().size
}
