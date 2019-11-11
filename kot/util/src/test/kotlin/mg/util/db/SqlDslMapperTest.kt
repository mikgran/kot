package mg.util.db

import mg.util.common.Common
import mg.util.db.SqlDslMapper.Companion.map
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SqlDslMapperTest {

    @Test
    fun testBuilding1() {

        val op = Sql select DBTest.PersonB() where DBTest.PersonB::firstName eq "name"

        val candidate = map(op.list())

        assertTrue(Common.hasContent(candidate))
        assertEquals("SELECT * FROM person12345 as p WHERE p.firstName = 'name'", candidate)
    }


}