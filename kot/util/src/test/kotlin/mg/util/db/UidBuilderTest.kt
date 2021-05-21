package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

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

        UidBuilder.uniqueIds = CountingHashMap()
        val putCounter = (UidBuilder.uniqueIds as? CountingHashMap)?.putCounter

        data class TestClass2(val someName2: String, val someInt: Int)

        assertNotNull(UidBuilder.uniqueIds)
        assertEquals(0, (putCounter?.get(TestClass2::class) ?: 0), "there should be 0 refs for ${TestClass2::class}")

        val candidate = UidBuilder.buildUniqueId(TestClass2("someName", 1))

        assertNotNull(candidate)
        assertEquals(buildUid("TestClass2", "someIntsomeName2"), candidate)
        assertTrue(UidBuilder.uniqueIds.contains(TestClass2::class))
        assertEquals(1, (putCounter?.get(TestClass2::class) ?: 0), "there should be 1 puts for ${TestClass2::class}")

        val candidate2 = UidBuilder.buildUniqueId(TestClass2("someName", 1))
        assertNotNull(candidate2)
        assertEquals(buildUid("TestClass2", "someIntsomeName2"), candidate2)
        assertEquals(1, (putCounter?.get(TestClass2::class) ?: 0), "there should be still 1 puts for ${TestClass2::class}")
    }

    private fun buildUid(str1: String, str2: String) = "$str1${str2.hashCode() and 0x7fffffff}"
}

// consider testing this class?
class CountingHashMap<K, V> : HashMap<K, V>() {

    @Synchronized
    private fun <T> synchronizedBlock(block: () -> T) = block()

    var putCounter = HashMap<K, Int>()
        get() = synchronizedBlock { field }
        set(value) = synchronizedBlock { field = value }

    override fun put(key: K, value: V): V? {
        var retVal: V? = null
        synchronizedBlock {
            putCounter[key] = putCounter.getOrDefault(key, 0) + 1
            retVal = super.put(key, value)
        }
        return retVal
    }
}


