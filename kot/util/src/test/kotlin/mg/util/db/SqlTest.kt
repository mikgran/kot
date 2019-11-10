package mg.util.db

import mg.util.db.DBTest.PersonB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SqlTest {

    // private val personB = PersonB("aa", "bb")

    // db.findBySql { select PersonB() where it::firstName eq "name" }
    // SELECT * FROM person12345 as p WHERE p.firstName = 'name'

    @Test
    fun testSqlBuilder() {

        // Sql select PersonB() where PersonB::firstName eq "name"
        // Sql.select(PersonB()).where(PersonB::firstName).eq("name")

        // val op = Sql select PersonB() where PersonB::firstName isNot "name"

        val op = Sql select PersonB() where PersonB::firstName eq "name"

        val list: MutableList<BuildingBlock> = op.list()

        assertTrue(list.isNotEmpty())
        assertEquals(PersonB(), (list[0] as SelectBlock<*>).type)
        assertEquals(PersonB::firstName, (list[1] as WhereBlock<*>).type)
        assertEquals("name", (list[2] as OperationBlock<*>).type)
        assertEquals("SELECT * FROM person12345 as p WHERE p.firstName = 'name'", op.build())
    }

}