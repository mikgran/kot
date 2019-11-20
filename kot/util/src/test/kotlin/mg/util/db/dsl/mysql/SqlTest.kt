package mg.util.db.dsl.mysql

import mg.util.db.DBTest
import mg.util.db.dsl.BuildingBlock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SqlTest {

    @Test
    fun testSqlBuilder() {

        val sql = Sql() select DBTest.PersonB() where DBTest.PersonB::firstName eq "name"

        val list: MutableList<BuildingBlock> = sql.list()

        assertTrue(list.isNotEmpty())
        assertEquals(DBTest.PersonB(), (list[0] as SelectBlock<*>).type)
        assertEquals(DBTest.PersonB::firstName, (list[1] as WhereBlock<*>).type)
        assertEquals("name", (list[2] as ValueBlock<*>).type)
    }

}
