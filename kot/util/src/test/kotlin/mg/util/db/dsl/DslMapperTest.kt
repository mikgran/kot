package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.PersonB
import mg.util.db.TestDataClasses
import mg.util.db.UidBuilder
import mg.util.db.UidBuilder.buildUniqueId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import mg.util.db.dsl.mysql.Sql as MySql
import mg.util.db.dsl.oracle.Sql as SqlOracle

internal class DslMapperTest {

    @Test
    fun testBuildingSqlFromDsl1() {

        val sql = MySql() select PersonB() where PersonB::firstName eq "name"

        val candidate = DslMapper.map(sql.list())

        val p = AliasBuilder.build(buildUniqueId(PersonB()))

        assertHasContent(candidate)
        assertEquals("SELECT $p.firstName, $p.lastName FROM PersonB608543900 $p WHERE $p.firstName = 'name'", candidate)
    }

    @Test
    fun testCreatingANewTable1() {

        val sql = MySql() create PersonB()

        val uid = UidBuilder.build(PersonB::class)
        val candidate = DslMapper.map(sql.list())

        assertEquals("CREATE TABLE IF NOT EXISTS $uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", candidate)
    }

    @Test
    fun testCreatingANewTableWithListReference() {

        val sql = MySql() create TestDataClasses.Building("some address")

        val buildingUid = UidBuilder.build(TestDataClasses.Building::class)
        val floorUid = UidBuilder.build(TestDataClasses.Floor::class)

        val candidate = DslMapper.map(sql.list())

        assertNotNull(candidate)
        assertEquals("CREATE TABLE IF NOT EXISTS $buildingUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "fullAddress VARCHAR(64) NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $floorUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "number MEDIUMINT NOT NULL);" +
                "ALTER TABLE $floorUid ADD COLUMN ${buildingUid}refId INT NOT NULL",
                candidate)
    }

    @Test
    fun testCreatingANewTableWithSimpleReference() {

        val sql = MySql() create TestDataClasses.Place(TestDataClasses.Address("somePlace"), 100000)

        val placeUid = UidBuilder.build(TestDataClasses.Place::class)
        val addressUid = UidBuilder.build(TestDataClasses.Address::class)
        val candidate = DslMapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $placeUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, rentInCents MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $addressUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, fullAddress VARCHAR(64) NOT NULL);" +
                "ALTER TABLE $addressUid ADD COLUMN ${placeUid}refId INT NOT NULL"

        assertNotNull(candidate)
        assertEquals(expected, candidate)
    }

    @Test
    fun testUpdate() {

        // UPDATE personb12345 SET field1 = new-value1, field2 = new-value2
        val sql = MySql() update PersonB() set PersonB::firstName eq "newFirstName" and PersonB::lastName eq "newLastName" where PersonB::firstName eq "firstName"

        val uid = UidBuilder.build(PersonB::class)

        val candidate = DslMapper.map(sql.list())

        val expected = "UPDATE $uid SET firstName = 'newFirstName', lastName = 'newLastName'" +
                " WHERE firstName = 'firstName'"

        assertNotNull(candidate)
        assertEquals(expected, candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoin() {

        // FIXME 10: "on a.f = b.f2", needs to be completed

        val sql = MySql() select TestDataClasses.Place() join TestDataClasses.Address()

        val candidate = DslMapper.map(sql.list())

        val p2 = buildUniqueId(TestDataClasses.Place())
        val a2 = buildUniqueId(TestDataClasses.Address())
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

    @Test
    fun testDslV2() {

        // MySql() select PersonB() where PersonB::firstName eq "name"

        val tt1: SQL2.Select.Where.Eq = SQL2 select PersonB() where PersonB::firstName eq "name"

        val tt2: SQL2.Update = SQL2 update PersonB()





    }
}
