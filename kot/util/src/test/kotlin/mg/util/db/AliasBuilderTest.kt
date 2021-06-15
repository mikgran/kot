package mg.util.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// NOTE: use only internal data class - so the static
// access object is NOT referred by any other tests.
internal class AliasBuilderTest {

    private fun `add three items`() {

        println(AliasBuilder.aliases())

        val candidate = AliasBuilder.build(Z1)
        println(AliasBuilder.aliases())
        assertEquals("z", candidate)

        val candidate2 = AliasBuilder.build(Z2)
        println(AliasBuilder.aliases())
        assertEquals("z2", candidate2)

        val candidate3 = AliasBuilder.build(Y)
        println(AliasBuilder.aliases())
        assertEquals("y", candidate3)

        // assert for storage
        val aliases = AliasBuilder.aliasCache()
        assertEquals("z", aliases["z"]!![Z1].toString())
        assertEquals("z2", aliases["z"]!![Z2].toString())
        assertEquals("y", aliases["y"]!![Y].toString())
    }

    private fun `deterministic, add an item twice`() {

        val candidate1 = AliasBuilder.build(Z2)
        assertEquals("z2", candidate1)

        val candidate2 = AliasBuilder.build(Z2)
        assertEquals("z2", candidate2)
    }

    @Test
    fun testBuildingAliases() {

        `add three items`()

        `deterministic, add an item twice`()
    }


    companion object {
        const val Z1 = "Zuto"
        const val Z2 = "ZutoB"
        const val Y = "Yarn"
    }
}