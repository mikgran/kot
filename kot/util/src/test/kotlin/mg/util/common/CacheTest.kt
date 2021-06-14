package mg.util.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class CacheTest {

    data class Simple(var variable: String = "")

    private val cache = Cache.of<String, List<Simple>>()
            .keyMapper { it.toString() }

    @Test
    fun testCache() {

        val newSimple = Simple("AAA")
        val newSimple2 = Simple("BBB")


//        val aValue = cache["some"]
//        cache["some"] = listOf(newSimple, newSimple2)

        fail("TODO")
    }

}