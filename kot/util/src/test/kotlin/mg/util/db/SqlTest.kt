package mg.util.db

import mg.util.db.DBTest.PersonB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SqlTest {

    @Test
    fun testSqlBuilder() {

        val sql = Sql select PersonB() where PersonB::firstName eq "name"

        val list: MutableList<BuildingBlock> = sql.list()

        assertTrue(list.isNotEmpty())
        assertEquals(PersonB(), (list[0] as SelectBlock<*>).type)
        assertEquals(PersonB::firstName, (list[1] as WhereBlock<*>).type)
        assertEquals("name", (list[2] as ValueBlock<*>).type)
    }

}