package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DslMapperTest {

    private val mapper = DslMapperFactory.get()

    @Test
    fun testCreatingANewTable() {

        val sql = Sql create PersonB()

        val uid = UidBuilder.build(PersonB::class)
        val candidate = mapper.map(sql)

        assertEquals("CREATE TABLE IF NOT EXISTS $uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", candidate)
    }

    @Test
    fun testCreatingANewTableWithListReference() {

        val sql = Sql create Building("some address")

        val buildingUid = UidBuilder.build(Building::class)
        val floorUid = UidBuilder.build(Floor::class)

        val candidate = mapper.map(sql)

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

        val sql = Sql create Place(Address("somePlace"), 100000)

        val placeUid = UidBuilder.build(Place::class)
        val addressUid = UidBuilder.build(Address::class)
        val candidate = mapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $placeUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, rentInCents MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $addressUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, fullAddress VARCHAR(64) NOT NULL);" +
                "ALTER TABLE $addressUid ADD COLUMN ${placeUid}refId INT NOT NULL"

        assertNotNull(candidate)
        assertEquals(expected, candidate)
    }

    @Test
    fun testUpdate() {

        // UPDATE personb12345 SET field1 = new-value1, field2 = new-value2
        val sql = Sql update PersonB() set PersonB::firstName eq "newFirstName" and PersonB::lastName eq "newLastName" where PersonB::firstName eq "firstName"

        val uid = UidBuilder.build(PersonB::class)

        val candidate = mapper.map(sql)

        val expected = "UPDATE $uid SET firstName = 'newFirstName', lastName = 'newLastName'" +
                " WHERE firstName = 'firstName'"

        assertNotNull(candidate)
        assertEquals(expected, candidate)


    }

    @Test
    fun testBuildingSqlFromDslJoin() {

        // FIXME 10: "on a.f = b.f2", needs to be completed

        val sql = Sql select Place() join Address()

        val candidate = mapper.map(sql)

        val p2 = UidBuilder.buildUniqueId(Place())
        val a2 = UidBuilder.buildUniqueId(Address())
        val p = AliasBuilder.build(p2)
        val a = AliasBuilder.build(a2)

        assertHasContent(candidate)
        assertEquals("SELECT $p.address, $p.rentInCents, $a.fullAddress FROM $p2 $p JOIN $a2 $a", candidate)
    }

    private fun assertHasContent(candidate: String) {
        assertTrue(hasContent(candidate), "no mapped content")
    }

    @Test
    fun testDsl1() {

        val sql = Sql select PersonB() where PersonB::firstName eq "name"

        val (uid, p) = buildUidAndAlias(PersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'name'"

        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        assertEquals(expected, candidate)

        // TOIMPROVE: test coverage
    }

    @Test
    fun testDsl2() {

        val sql = Sql select PersonB() where PersonB::firstName eq "first" and PersonB::lastName eq "last"

        val (uid, p) = buildUidAndAlias(PersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'first' AND $p.lastName = 'last'"

        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        assertEquals(expected, candidate)
    }

    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val p = AliasBuilder.build(uid)
        return uid to p
    }

}
