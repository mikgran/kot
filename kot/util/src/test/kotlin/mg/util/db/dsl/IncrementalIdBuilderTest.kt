package mg.util.db.dsl

import mg.util.common.Common.classSimpleName
import mg.util.common.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class IncrementalIdBuilderTest {

    private fun IncrementalNumberBuilder.incBuilderContentSize() = cache().contents().size

    @Test
    fun testNext() {

        val incBuilder = IncrementalNumberBuilder()

        data class TestClass1(var s: String = "s", var s2: String = "s")
        data class SomeTestClass2(var s: String = "s")

        val className = TestClass1().classSimpleName()
        var numberCandidate: Int?

        assertEquals(0, incBuilder.incBuilderContentSize())

        numberCandidate = incBuilder.next(className)

        assertEquals(1, numberCandidate)
        assertEquals(1, incBuilder.incBuilderContentSize())

        numberCandidate = incBuilder.next(className)

        assertEquals(2, numberCandidate)
        assertEquals(1, incBuilder.incBuilderContentSize())

        val className2 = SomeTestClass2().classSimpleName()
        numberCandidate = incBuilder.next(className2)

        assertEquals(1, numberCandidate)
        assertEquals(2, incBuilder.incBuilderContentSize())

        val expectedMap = mutableMapOf(className to 2, className2 to 1)
        val contents = incBuilder.cache().contents()
        assertTrue(contents.entries.containsAll(expectedMap.entries))

        assertEquals(2, incBuilder[className])
        assertEquals(1, incBuilder[className2])
    }

    @Test
    fun testNextNamed() {

        val incBuilder = IncrementalNumberBuilder()
        val str = "string"
        var candidate: String? = incBuilder.nextNamed(str)

        TestUtil.expect("${str}1", candidate)

        candidate = incBuilder.nextNamed(str)

        TestUtil.expect("${str}2", candidate)
    }

    @Test
    fun testGetNamed() {

        val str = "string"
        val incBuilder = IncrementalNumberBuilder()

        incBuilder.next(str)
        incBuilder.next(str)

        var candidate: String? = incBuilder.getNamed(str)

        TestUtil.expect("${str}2", candidate)

        candidate = incBuilder.getNamed("string2")

        assertNull(candidate)
    }


}
