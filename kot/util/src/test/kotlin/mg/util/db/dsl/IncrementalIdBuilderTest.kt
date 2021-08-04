package mg.util.db.dsl

import mg.util.common.Common.classSimpleName
import mg.util.common.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class IncrementalIdBuilderTest {

    private fun IncrementalNumberBuilder.nextIncBuilderContentSize() = cache().contents().size

    @Test
    fun testBuildingLastId() {

        val incBuilder = IncrementalNumberBuilder()

        data class TestClass1(var s: String = "s", var s2: String = "s")
        data class SomeTestClass2(var s: String = "s")

        val className = TestClass1().classSimpleName()
        var idCandidate: Int?

        assertEquals(0, incBuilder.nextIncBuilderContentSize())

        idCandidate = incBuilder.next(className)

        assertEquals(1, idCandidate)
        assertEquals(1, incBuilder.nextIncBuilderContentSize())

        idCandidate = incBuilder.next(className)

        assertEquals(2, idCandidate)
        assertEquals(1, incBuilder.nextIncBuilderContentSize())

        val className2 = SomeTestClass2().classSimpleName()
        idCandidate = incBuilder.next(className2)

        assertEquals(1, idCandidate)
        assertEquals(2, incBuilder.nextIncBuilderContentSize())

        val expectedMap = mutableMapOf(className to 2, className2 to 1)
        val contents = incBuilder.cache().contents()
        assertTrue(contents.entries.containsAll(expectedMap.entries))

        assertEquals(2, incBuilder[className])
        assertEquals(1, incBuilder[className2])
    }

    @Test
    fun testId() {

        val incBuilder = IncrementalNumberBuilder()
        val str = "string"
        var candidate: String? = incBuilder.inc(str)

        TestUtil.expect("${str}1", candidate)

        candidate = incBuilder.inc(str)

        TestUtil.expect("${str}2", candidate)
    }


}
