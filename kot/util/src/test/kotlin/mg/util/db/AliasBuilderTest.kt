package mg.util.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AliasBuilderTest {

    @Test
    fun testAlias1() {

        val candidate = AliasBuilder.alias("lastName")
        assertEquals("l", candidate)

        val candidate2 = AliasBuilder.alias("lastDate")
        assertEquals("l2", candidate2)

        val candidate3 = AliasBuilder.alias("firstName")
        assertEquals("f", candidate3)

        // assert for storage
        assertEquals("{f={firstName=f}, l={lastName=l, lastDate=l2}}", AliasBuilder.toString())
    }
}