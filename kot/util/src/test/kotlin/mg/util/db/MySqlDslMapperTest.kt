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

        assertTrue(hasContent(candidate), "no mapped content")
        // assertEquals("SELECT p.firstName, p.lastName FROM person12345 p WHERE p.firstName = 'name'", candidate)
    }


}