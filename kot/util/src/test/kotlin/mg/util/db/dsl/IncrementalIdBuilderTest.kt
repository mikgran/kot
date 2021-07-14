package mg.util.db.dsl

import mg.util.common.Common.classSimpleName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class IdBuilderTest {

    @Test
    fun testBuildingLastId() {

        data class TestClass1(var s: String = "s", var s2: String = "s")
        data class SomeTestClass2(var s: String = "s")

        val className = TestClass1().classSimpleName()
        var idCandidate: Int?

        assertEquals(0, nextIdBuilderContentSize())

        idCandidate = IncrementalIdBuilder.next(className)

        assertEquals(1, idCandidate)
        assertEquals(1, nextIdBuilderContentSize())

        idCandidate = IncrementalIdBuilder.next(className)

        assertEquals(2, idCandidate)
        assertEquals(1, nextIdBuilderContentSize())

        val className2 = SomeTestClass2().classSimpleName()
        idCandidate = IncrementalIdBuilder.next(className2)

        assertEquals(1, idCandidate)
        assertEquals(2, nextIdBuilderContentSize())

        val expectedMap = mutableMapOf(className to 2, className2 to 1)
        val contents = IncrementalIdBuilder.contents().contents()
        assertTrue(contents.entries.containsAll(expectedMap.entries))

        assertEquals(2, IncrementalIdBuilder[className])
        assertEquals(1, IncrementalIdBuilder[className2])
    }

    private fun nextIdBuilderContentSize() = IncrementalIdBuilder.contents().contents().size
}
