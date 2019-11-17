package mg.util.db

import mg.util.common.Common.hasContent
import mg.util.db.DBTest.PersonB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MySqlDslMapperTest {

    private data class Address(val fullAddress: String = "", val streetName: String = "", val streetNumber: Int = 0)
    private data class Place(val address: Address = Address(), val rentInCents: Int = 0)

    @Test
    fun testBuildingSqlFromDsl1() {

        val sql = Sql select PersonB() where PersonB::firstName eq "name"

        val candidate = MySqlDslMapper.map(sql.list())

        println("candidate: $candidate")

        assertHasContent(candidate)
        assertEquals("SELECT p.firstName, p.lastName FROM PersonB608543900 p WHERE p.firstName = 'name'", candidate)
    }

    @Test
    fun testBuildingSqlFromDsl2() {

        val sql = Sql select Place() join Address()

        val candidate = MySqlDslMapper.map(sql.list())

        val dbo = DBO(SqlMapperFactory.get("mysql"))
        val p = AliasBuilder.alias(dbo.buildUniqueId(Place()))
        val a = AliasBuilder.alias(dbo.buildUniqueId(Address()))

        println("p:: $p")
        println("a:: $a")

        assertHasContent(candidate)
        assertEquals("SELECT $p.address, $a.fullAddress, $a.streetName," +
                " $a.streetNumber FROM Place $p JOIN Address $a", candidate)
    }

    private fun assertHasContent(candidate: String) {
        assertTrue(hasContent(candidate), "no mapped content")
    }
}