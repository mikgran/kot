package mg.util.db

import mg.util.common.Common.hasContent
import mg.util.db.DBTest.PersonB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MySqlDslMapperTest {

    @Test
    fun testBuilding1() {

        val op = Sql select PersonB() where PersonB::firstName eq "name"

        val candidate = MySqlDslMapper.map(op.list())

        assertTrue(hasContent(candidate))
        assertEquals("SELECT * FROM person12345 p1 WHERE p1.firstName = 'name'", candidate)
    }


}