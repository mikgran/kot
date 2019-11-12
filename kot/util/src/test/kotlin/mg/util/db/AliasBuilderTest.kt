package mg.util.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AliasBuilderTest {

    @Test
    fun testAlias1() {

        val lastName = "lastName"
        val lastDate = "lastDate"

        val candidate = AliasBuilder.alias(lastName)

        assertEquals("l", candidate)

        val candidate2 = AliasBuilder.alias(lastDate)

        assertEquals("l2", candidate2)

    }
}