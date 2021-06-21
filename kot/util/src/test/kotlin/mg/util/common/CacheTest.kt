package mg.util.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CacheTest {

    data class Simple(var variable: String = "")

    private val cache = Cache.of<String, List<Simple>>()

    @Test
    fun testCache() {

        val simple1 = Simple("AAA")
        val simple2 = Simple("BBB")
        val list1 = listOf(simple1, simple2)

        assertFalse(cache.cache().containsKey("key1"))

        cache["key1"] = list1

        assertTrue(cache.cache().containsKey("key1"))

        val candidate = cache["key1"]

        assertNotNull(candidate)
        assertEquals(2, candidate?.size)
        assertEquals(simple1, candidate?.get(0))
        assertEquals(simple2, candidate?.get(1))

        assertFalse(cache.cache().containsKey("key2"))

        val simple3 = Simple("CCC")
        val candidate2 = cache.getOrCache("key2") { listOf(simple3) }

        assertNotNull(candidate2)
        assertEquals(1, candidate2.size)
        assertEquals(simple3, candidate2[0])
        val cache1 = cache.cache()
        assertTrue(cache1.containsKey("key1"))
        assertTrue(cache1.containsKey("key2"))

        cache.replaceWith(mutableMapOf())

        assertTrue(cache.cache().isEmpty())
    }
}
