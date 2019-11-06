package mg.util.db

import mg.util.db.DBTest.PersonB
import org.junit.jupiter.api.Test

internal class SqlTest {

    val personB = PersonB("aa", "bb")

    // db.findBySql { select PersonB() where it::firstName eq "name" }
    // SELECT * FROM person12345 as p WHERE p.firstName = 'name'

    @Test
    fun testSqlBuilder() {

        // Sql select PersonB() where PersonB::firstName eq "name"
        // Sql.select(PersonB()).where(PersonB::firstName).eq("name")

        // val op = Sql select PersonB() where PersonB::firstName isNot "name"

        val op = Sql select PersonB() where PersonB::firstName eq "name"

        val list: MutableList<BuildingBlock> = op.getList()

        list.forEach(::println)

    }

}