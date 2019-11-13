package mg.util.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// NOTE: use only internal data class - so the static
// access object is NOT referred by any other tests.
internal class AliasBuilderTest {

    private fun `add three items`() {

        val candidate = AliasBuilder.alias("Zuto")
        assertEquals("Z", candidate)

        val candidate2 = AliasBuilder.alias("ZutoB")
        assertEquals("Z2", candidate2)

        val candidate3 = AliasBuilder.alias("Yarn")
        assertEquals("Y", candidate3)

        println(AliasBuilder.toString())
        // assert for storage
        assertEquals("{Y={Yarn=Y}, Z={ZutoB=Z2, Zuto=Z}}", AliasBuilder.toString())
    }

    private fun `deterministic, add an item twice`() {

        val candidate1 = AliasBuilder.alias("ZutoB")
        assertEquals("Z2", candidate1)

        val candidate2 = AliasBuilder.alias("ZutoB")
        assertEquals("Z2", candidate2)
    }

    @Test
    fun testBuildingAliases() {

        `add three items`()

        `deterministic, add an item twice`()
    }

}