package mg.util.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CacheTest {

    data class Simple(var variable: String = "")

    @Test
    fun testCache() {

        val cache = Cache.cacheOf<List<Simple>> {



        }

        cache["some"]


    }

}