package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UidBuilderTest {

    @Test
    fun testBuild() {

        data class TestClass(val someName: String, val someInt: Int)

        val candidate = UidBuilder.buildUniqueId(TestClass("someName", 1))

        assertNotNull(candidate)
        assertEquals(buildUid("TestClass", "someIntsomeName"), candidate)
    }

    @Test
    fun testCachedBuild() {

        data class TestClass2(val someName2: String, val someInt: Int)

        val cache1 = UidBuilder.uids()
        assertNotNull(cache1)
        assertNull(cache1[TestClass2::class])
        assertEquals(0, UidBuilder.uids().cache().keys.filter { it == TestClass2::class } .size)

        val candidate = UidBuilder.buildUniqueId(TestClass2("someName", 1))

        assertTrue(UidBuilder.uids().cache().containsKey(TestClass2::class))
        assertEquals(1, UidBuilder.uids().cache().keys.filter { it == TestClass2::class } .size)
        assertNotNull(candidate)
        assertEquals(buildUid("TestClass2", "someIntsomeName2"), candidate)

        val candidate2 = UidBuilder.buildUniqueId(TestClass2("someName", 1))

        assertTrue(UidBuilder.uids().cache().containsKey(TestClass2::class))
        assertEquals(1, UidBuilder.uids().cache().keys.filter { it == TestClass2::class } .size)
        assertNotNull(candidate2)
        assertEquals(buildUid("TestClass2", "someIntsomeName2"), candidate2)
    }

    private fun buildUid(str1: String, str2: String) = "$str1${str2.hashCode() and 0x7fffffff}"
}



