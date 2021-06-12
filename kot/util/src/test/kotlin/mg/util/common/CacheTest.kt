package mg.util.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class CacheTest {

    data class Simple(var variable: String = "")

    @Test
    fun testCache() {

        val cache = Cache.cacheOf<String, List<Simple>> {

        

        }

        cache["some"]

        fail("TODO")
    }

}