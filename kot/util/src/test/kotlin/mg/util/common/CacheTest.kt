package mg.util.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CacheTest {

    data class Simple(var variable: String = "")

    private val cache = Cache.of<String, List<Simple>>()
            .keyMapper { it.toString() }

    @Test
    fun testCache() {

        val simple1 = Simple("AAA")
        val simple2 = Simple("BBB")
        val list1 = listOf(simple1, simple2)

        cache["key1"] = list1

        assertTrue(cache.cache.containsKey("key1"))

        val candidate = cache["key1"]

        assertNotNull(candidate)
        assertTrue(candidate?.size == 2)
        assertEquals(simple1, candidate?.get(0))
    }
}
