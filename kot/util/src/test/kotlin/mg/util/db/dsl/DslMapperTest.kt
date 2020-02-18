package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
        val alias = AliasBuilder.build(uid)

        val candidate = mapper.map(sql)

        val expected = "UPDATE $uid $alias SET firstName = 'newFirstName', lastName = 'newLastName'" +
                " WHERE $alias.firstName = 'firstName'"

        assertNotNull(candidate)
        expect(expected, candidate)
    }

    private fun <T : Any> expect(expected: T, candidate: T) {
        if (expected != candidate) {
            println("\nexpected:\n<$expected>")
            println("\ncandidate:\n<$candidate>")
        }
        assertEquals(expected, candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoin_AutoRef() {
        // SELECT p.address, p.rentInCents, a.fullAddress FROM Place1234556 p
        // JOIN Address123565 a ON p.id = a.Place1234556refid
        val dsl = Sql select Place()
        val candidate = mapper.map(dsl)

        assertDslForAutoRef(candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoin_GivenRef() {

        // SELECT p.id, p.rentInCents,
        //        a.id, a.fullAddress, a.Place1234556refid,
        //        p2.id, p2.description, p2.Place1234556refid
        // FROM
        //        Place1234556 p
        // JOIN
        //        Address123565 a ON p.id = a.Place1234556refid
        // JOIN
        //        PlaceDescriptor123456 p2 ON p.id = p2.Place1234556refId

        // ResultSet:
        // p.id, p.rentincents, a.id, a.fulladdress, a.placerefid, p2.id, p2.description, p2.placerefid
        // 1     150000         15    StreetName     1             2053   Some Desc       1
        // 2     160000         16    A Street       2             6342   Something..     2

        val dsl = Sql select Place() join PlaceDescriptor() on Place::class eq PlaceDescriptor::placeRefId
        val candidate = mapper.map(dsl)

        assertDslForManualField(candidate)
    }

    private fun assertDslForManualField(candidate: String) {
        // SELECT p.description, p.placeRefId, p2.address, p2.rentInCents, a.fullAddress
        // FROM Place536353721 p2
        // JOIN PlaceDescriptor1660249411 p ON p2.id = p.placeRefId
        // JOIN Address2002641509 a ON p2.id = a.Place536353721refid
        val (uidPlace, p) = buildUidAndAlias(Place())
        val (uidAddress, a) = buildUidAndAlias(Address())
        val (uidDesc, p2) = buildUidAndAlias(PlaceDescriptor())
        val expected = "SELECT $p2.description, ${p2}.placeRefId, $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $uidPlace $p" +
                " JOIN $uidDesc $p2 ON $p.id = $p2.placeRefId" +
                " JOIN $uidAddress $a ON $p.id = $a.${uidPlace}RefId"

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    private fun assertDslForAutoRef(candidate: String) {
        val (uidPlace, p) = buildUidAndAlias(Place())
        val (uidAddress, a) = buildUidAndAlias(Address())
        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $uidPlace $p JOIN $uidAddress $a" +
                " ON $p.id = $a.${uidPlace}RefId"

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    private fun assertHasContent(candidate: String) {
        assertTrue(hasContent(candidate), "no mapped content")
    }

    @Test
    fun testDslSelectWhere() {

        val sql = Sql select PersonB() where PersonB::firstName eq "name"

        val (uid, p) = buildUidAndAlias(PersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'name'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    @Test
    fun testDslSelectWhereAndWhere() {

        val sql = Sql select PersonB() where PersonB::firstName eq "first" and PersonB::lastName eq "last"

        val (uid, p) = buildUidAndAlias(PersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'first' AND $p.lastName = 'last'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        assertEquals(expected, candidate)
    }

    @Test
    fun testDslDelete() {

        val uid = UidBuilder.buildUniqueId(Person())
        val dsl = Sql delete Person() where Person::firstName eq "Something"

        val sql = mapper.map(dsl)

        expect("DELETE FROM $uid WHERE firstName = 'Something'", sql)
    }

    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val p = AliasBuilder.build(uid)
        return uid to p
    }
}
