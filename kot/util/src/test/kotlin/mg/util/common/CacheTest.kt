package mg.util.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class CacheTest {

    data class Simple(var variable: String = "")

    @Test
    fun testCache() {

        Cache.cacheOf("key", listOf(Simple()))


//        cache["some"]
//        cache["some"] = listOf(Simple())

        fail("TODO")
    }

}