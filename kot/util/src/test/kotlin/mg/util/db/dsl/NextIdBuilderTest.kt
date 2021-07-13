package mg.util.db.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NextIdBuilderTest {

    @Test
    fun testBuildingLastId() {

        data class TestClass1(var s: String = "s", var s2: String = "s")
        val className = "" + TestClass1::class.simpleName
        var idCandidate: String?

        assertEquals(0, nextIdBuilderContentSize())

        idCandidate = NextIdBuilder.build(className)

        assertEquals("TestClass11", idCandidate)
        assertEquals(1, nextIdBuilderContentSize())

        idCandidate = NextIdBuilder.build(className)

        assertEquals("TestClass12", idCandidate)
        assertEquals(2, nextIdBuilderContentSize())


    }

    private fun nextIdBuilderContentSize() = NextIdBuilder.contents().contents().size

}