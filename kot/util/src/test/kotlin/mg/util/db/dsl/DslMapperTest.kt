package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.DBO
import mg.util.db.DBTest.PersonB
import mg.util.db.SqlMapperFactory
import mg.util.db.dsl.mysql.Sql as SqlMysql
import mg.util.db.dsl.oracle.Sql as SqlOracle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DslMapperTest {

    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    private data class Address(val fullAddress: String = "")
    private data class Place(val address: Address = Address(), val rentInCents: Int = 0)

    @Test
    fun testBuildingSqlFromDsl1() {

        val sql = SqlMysql() select PersonB() where PersonB::firstName eq "name"

        val candidate = DslMapper.map(sql.list())

        val p = AliasBuilder.alias(dbo.buildUniqueId(PersonB()))

        assertHasContent(candidate)
        assertEquals("SELECT $p.firstName, $p.lastName FROM PersonB608543900 $p WHERE $p.firstName = 'name'", candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoin() {

        // TODO 1: fix join on a.f = b.f2

        val sql = SqlMysql() select Place() join Address()

        val candidate = DslMapper.map(sql.list())

        val p2 = dbo.buildUniqueId(Place())
        val a2 = dbo.buildUniqueId(Address())
        val p = AliasBuilder.alias(p2)
        val a = AliasBuilder.alias(a2)

        assertHasContent(candidate)
        assertEquals("SELECT $p.address, $p.rentInCents, $a.fullAddress FROM $p2 $p JOIN $a2 $a", candidate)
    }

    private fun assertHasContent(candidate: String) {
        assertTrue(hasContent(candidate), "no mapped content")
    }

    @Test
    fun testOracleSqlSelect() {

        val sql = SqlOracle() select PersonB() where PersonB::firstName eq "name"

        val candidate = DslMapper.map(sql.list())

        val p = AliasBuilder.alias(dbo.buildUniqueId(PersonB()))

        assertHasContent(candidate)
        assertEquals("SELECT $p.firstName, $p.lastName FROM PersonB608543900 AS $p WHERE $p.firstName = 'name'", candidate)
    }

}