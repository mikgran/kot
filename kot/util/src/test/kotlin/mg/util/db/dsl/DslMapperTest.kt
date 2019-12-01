package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.DBO
import mg.util.db.DBTest.PersonB
import mg.util.db.SqlMapperFactory
import mg.util.db.UidBuilder
import mg.util.db.UidBuilder.buildUniqueId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import mg.util.db.dsl.mysql.Sql as SqlMysql
import mg.util.db.dsl.oracle.Sql as SqlOracle

internal class DslMapperTest {

    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    private data class Address(val fullAddress: String = "")
    private data class Place(val address: Address = Address(), val rentInCents: Int = 0)

    @Test
    fun testBuildingSqlFromDsl1() {

        val sql = SqlMysql() select PersonB() where PersonB::firstName eq "name"

        val candidate = DslMapper.map(sql.list())

        val p = AliasBuilder.build(buildUniqueId(PersonB()))

        assertHasContent(candidate)
        assertEquals("SELECT $p.firstName, $p.lastName FROM PersonB608543900 $p WHERE $p.firstName = 'name'", candidate)
    }

    @Test
    fun testCreatingANewTable1() {

        val sql = SqlMysql() create PersonB()

        val uid = UidBuilder.build(PersonB::class)
        val candidate = DslMapper.map(sql.list())

        assertEquals("CREATE TABLE IF NOT EXISTS $uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", candidate)
    }

    @Test
    fun testCreatingANewTableWithSimpleReference() {

        // val sql = SqlMysql() create

    }


    @Test
    fun testBuildingSqlFromDslJoin() {

        // TODO 1: "on a.f = b.f2"

        val sql = SqlMysql() select Place() join Address()

        val candidate = DslMapper.map(sql.list())

        val p2 = buildUniqueId(Place())
        val a2 = buildUniqueId(Address())
        val p = AliasBuilder.build(p2)
        val a = AliasBuilder.build(a2)

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

        val uid = buildUniqueId(PersonB())
        val p = AliasBuilder.build(uid)

        assertHasContent(candidate)
        assertEquals("SELECT $p.firstName, $p.lastName FROM $uid AS $p WHERE $p.firstName = 'name'", candidate)
    }

}