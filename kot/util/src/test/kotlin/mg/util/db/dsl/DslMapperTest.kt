package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1

// TOIMPROVE: test coverage
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
                "ALTER TABLE $floorUid ADD COLUMN ${buildingUid}refId MEDIUMINT(9) NOT NULL",
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
                "ALTER TABLE $addressUid ADD COLUMN ${placeUid}refId MEDIUMINT(9) NOT NULL"

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

        // SELECT p.address, p.rentInCents, a.fullAddress FROM Place1234556 p
        // JOIN Address123565 a ON p.id = a.Place1234556refid
        val dslAutomaticJoin = Sql select Place()
        val candidateDslJoin = mapper.map(dslAutomaticJoin)

        // TODO 102 assertDsl1(candidateDslJoin)
        println(candidateDslJoin)

        // SELECT p.address, p.rentInCents, a.fullAddress FROM Place1234556 p
        // JOIN Address123565 a ON p.id = a.Place1234556refid
        val dslManualJoin = Sql select Place() join Address()
        val candidateDslManualJoin = mapper.map(dslManualJoin)

        assertDsl1(candidateDslManualJoin)

        // SELECT p.address, p.rentInCents, a.fullAddress FROM Place1234556 p
        // JOIN Address123565 a ON p.id = a.Place1234556refid
        // JOIN PlaceDescriptor123456 p2 ON p.id = p2.placeRefId
        val dslManualJoinWithSpecificField = Sql select Place() join PlaceDescriptor() on Place::class eq PlaceDescriptor::placeRefId
        val candidateDslManualJoinWithSpecificField = mapper.map(dslManualJoinWithSpecificField)

        // TODO 101 assertDsl2(candidate3)
        println(candidateDslManualJoinWithSpecificField)
    }

    private fun assertDsl2(candidate: String) {
        val (uidPlace, p) = buildUidAndAlias(Place())
        val (uidAddress, a) = buildUidAndAlias(PlaceDescriptor())
        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $uidPlace $p JOIN $uidAddress $a" +
                " ON $p.id = $a.placeRefId"
        assertHasContent(candidate)
        assertEquals(expected, candidate)
    }

    private fun assertDsl1(candidate: String) {
        val (uidPlace, p) = buildUidAndAlias(Place())
        val (uidAddress, a) = buildUidAndAlias(Address())
        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $uidPlace $p JOIN $uidAddress $a" +
                " ON $p.id = $a.${uidPlace}refid"
        assertHasContent(candidate)
        assertEquals(expected, candidate)
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
