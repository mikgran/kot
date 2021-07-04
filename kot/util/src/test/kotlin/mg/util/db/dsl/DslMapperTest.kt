package mg.util.db.dsl

import mg.util.common.Common
import mg.util.common.TestUtil
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.exp

// FIXME: 50 Fix all select, select-join, multitable inserts, creates
internal class DslMapperTest {

    private val mapper = DslMapperFactory.get()

    private fun assertHasContent(candidate: String) {
        assertTrue(Common.hasContent(candidate), "no mapped content")
    }

    @Test
    fun testCreatingANewTable() {

        val sql = Sql create DSLPersonB()
        val uid = UidBuilder.build(DSLPersonB::class)
        val candidate = mapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)"
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToManyRelation() {

        val sql = Sql create DSLBuilding("some address", listOf(DSLFloor(1)))
        val buildingUid = UidBuilder.build(DSLBuilding::class)
        val floorUid = UidBuilder.build(DSLFloor::class)

        val candidate = mapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $buildingUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, fullAddress VARCHAR(64) NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $floorUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, number MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $buildingUid$floorUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ${buildingUid}refid MEDIUMINT NOT NULL, ${floorUid}refid MEDIUMINT NOT NULL)"

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToManyMultipleDepthRelation() {

        val dslAddressM1 = DSLAddressM1(10, DSLLocationM1("TTTT", listOf(DSLPersonM1("VVVV"))))
        val sql = Sql create dslAddressM1

        val addressM1Uid = UidBuilder.build(DSLAddressM1::class)
        val locationM1Uid = UidBuilder.build(DSLLocationM1::class)
        val personM1Uid = UidBuilder.build(DSLPersonM1::class)

        val expected = "CREATE TABLE IF NOT EXISTS $addressM1Uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, rent MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $locationM1Uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, loc VARCHAR(64) NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $addressM1Uid$locationM1Uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ${addressM1Uid}refid MEDIUMINT NOT NULL, ${locationM1Uid}refid MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $personM1Uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(64) NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $locationM1Uid$personM1Uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ${locationM1Uid}refid MEDIUMINT NOT NULL, ${personM1Uid}refid MEDIUMINT NOT NULL)"

        val candidate = mapper.map(sql)

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToOneRelation() {

        val sql = Sql create DSLPlace(DSLAddress("somePlace"), 100000)
        val placeUid = UidBuilder.build(DSLPlace::class)
        val addressUid = UidBuilder.build(DSLAddress::class)

        val candidate = mapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $placeUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, rentInCents MEDIUMINT NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $addressUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, fullAddress VARCHAR(64) NOT NULL);" +
                "CREATE TABLE IF NOT EXISTS $placeUid$addressUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ${placeUid}refid MEDIUMINT NOT NULL, ${addressUid}refid MEDIUMINT NOT NULL)"

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testInsertNoCustomRelations() {

        val sql = Sql insert DSLPerson("first", "last")

        val (personUid, _) = buildUidAndAlias(DSLPerson())

        val expected = "INSERT INTO $personUid (firstName, lastName) VALUES ('first', 'last')"

        val candidate = mapper.map(sql)

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testInsertOneToOneRelation() {

        val dslAddress2 = DSLAddress2("anAddress")
        val dslPlace2 = DSLPlace2(dslAddress2, rentInCents = 80000)

        val dslPlace2Uid = UidBuilder.buildUniqueId(dslPlace2)
        val dslAddress2Uid = UidBuilder.buildUniqueId(dslAddress2)
        val tableJoinUid = dslPlace2Uid + dslAddress2Uid

        val sql = Sql insert dslPlace2

        val candidate: String = mapper.map(sql)

        // TOIMPROVE: if two inserts that create id hit at the same time with same column values this may fail, replace with lastid()?
        val expected = "INSERT INTO $dslPlace2Uid (rentInCents) VALUES ('80000');" +
                "SELECT LAST_INSERT_ID() INTO @parentLastId;" +
                "INSERT INTO $dslAddress2Uid (fullAddress) VALUES ('anAddress');" +
                "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                "INSERT INTO $tableJoinUid (${dslPlace2Uid}refid, ${dslAddress2Uid}refid) VALUES (@parentLastId, @childLastId)"

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testInsertOneToManyRelation() {

        val dslAddress3 = DSLAddress3("anAddress")
        val dslFloors3 = listOf(DSLFloor3(10), DSLFloor3(20))
        val dslPlace3 = DSLPlace3(dslAddress3, dslFloors3, rentInCents = 80000)

        val dslAddress3Uid = UidBuilder.buildUniqueId(dslAddress3)
        val dslPlace3Uid = UidBuilder.buildUniqueId(dslPlace3)
        val dslFloor3Uid = UidBuilder.buildUniqueId(DSLFloor3())
        val placeAddressJoinUid = dslPlace3Uid + dslAddress3Uid
        val placeFloorJoinUid = dslPlace3Uid + dslFloor3Uid

        val sql = Sql insert dslPlace3
        val candidate: String = mapper.map(sql)

        // TOIMPROVE: if two inserts that create id hit at the same time with same column values this may fail, replace with lastid()?
        val expected = "INSERT INTO $dslPlace3Uid (rentInCents) VALUES ('80000');" +
                "SELECT LAST_INSERT_ID() INTO @parentLastId;" +
                "INSERT INTO $dslAddress3Uid (fullAddress) VALUES ('anAddress');" +
                "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                "INSERT INTO $placeAddressJoinUid (${dslPlace3Uid}refid, ${dslAddress3Uid}refid) VALUES (@parentLastId, @childLastId);" +
                "INSERT INTO $dslFloor3Uid (number) VALUES ('10');" +
                "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                "INSERT INTO $placeFloorJoinUid (${dslPlace3Uid}refid, ${dslFloor3Uid}refid) VALUES (@parentLastId, @childLastId);" +
                "INSERT INTO $dslFloor3Uid (number) VALUES ('20');" +
                "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                "INSERT INTO $placeFloorJoinUid (${dslPlace3Uid}refid, ${dslFloor3Uid}refid) VALUES (@parentLastId, @childLastId)"

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testUpdate() {

        // UPDATE personb12345 SET field1 = new-value1, field2 = new-value2
        val sql = Sql update DSLPersonB() set DSLPersonB::firstName eq "newFirstName" and DSLPersonB::lastName eq "newLastName" where DSLPersonB::firstName eq "firstName"

        val uid = UidBuilder.build(DSLPersonB::class)
        val alias = AliasBuilder.build(uid)

        val candidate = mapper.map(sql)

        val expected = "UPDATE $uid $alias SET firstName = 'newFirstName', lastName = 'newLastName'" +
                " WHERE $alias.firstName = 'firstName'"

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoinByNaturalReference() {
        /*
            SELECT
                d9.address, d9.rentInCents, d10.fullAddress
            FROM
                DSLPlace536353721 d9
            JOIN
                DSLPlace536353721DSLAddress2002641509 d14 ON d14.DSLPlace536353721refid = d9.id
            JOIN
                DSLAddress2002641509 d10 ON d14.DSLAddress2002641509refid = d10.id
         */
        val dsl = Sql select DSLPlace()
        val candidate = mapper.map(dsl)

        val (placeUid, p) = buildUidAndAlias(DSLPlace())
        val (addressUid, a) = buildUidAndAlias(DSLAddress())
        val joinTableAlias = AliasBuilder.build("$placeUid$addressUid")

        val expected = "SELECT $p.rentInCents, $a.fullAddress" +
                " FROM $placeUid $p" +
                " JOIN $placeUid$addressUid $joinTableAlias ON $joinTableAlias.${placeUid}refid = $p.id" +
                " JOIN $addressUid $a ON ${joinTableAlias}.${addressUid}refid = $a.id"

        assertHasContent(candidate)
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoinByTwoNaturalRefs() {
        /*
            SELECT
                d9.rentInCents, d10.fullAddress
            FROM
                DSLPlace536353721 d9
            JOIN
                DSLPlace536353721DSLAddress2002641509 d14 ON d14.DSLPlace536353721refid = d9.id
            JOIN
                DSLAddress2002641509 d10 ON d14.DSLAddress2002641509refid = d10.id
            JOIN
                ...
            JOIN
                ...
         */

        val dsl = Sql select DSLPlace3()
        val candidate = mapper.map(dsl)

        val (placeUid, p) = buildUidAndAlias(DSLPlace3())
        val (addressUid, a) = buildUidAndAlias(DSLAddress3())
        val (floorUid, f) = buildUidAndAlias(DSLFloor3())
        val pa = AliasBuilder.build("$placeUid$addressUid")
        val pf = AliasBuilder.build("$placeUid$floorUid")

        val expected = "SELECT " +
                "$p.rentInCents, $a.fullAddress, $f.number" +
                " FROM $placeUid $p" +
                " JOIN $placeUid$addressUid $pa ON $pa.${placeUid}refid = $p.id" +
                " JOIN $addressUid $a ON ${pa}.${addressUid}refid = $a.id" +
                " JOIN $placeUid$floorUid $pf ON $pf.${placeUid}refid = $p.id" +
                " JOIN $floorUid $f ON ${pf}.${floorUid}refid = $f.id"

        assertHasContent(candidate)
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testBuildingSqlFromDslJoinByGivenReference() {
        /*
            SELECT
                d9.rentInCents, d10.fullAddress
            FROM
                DSLPlace536353721 d9
            JOIN
                DSLPlace536353721DSLAddress2002641509 d14 ON d14.DSLPlace536353721refid = d9.id
            JOIN
                DSLAddress2002641509 d10 ON d14.DSLAddress2002641509refid = d10.id
            JOIN
                DSLPlaceDescriptor1660249411 d15 ON d9.id = d15.placerefid
         */

        val dsl = Sql select DSLPlace() join DSLPlaceDescriptor() on DSLPlace::class eq DSLPlaceDescriptor::placeRefId
        val candidate = mapper.map(dsl)

        val (placeUid, p) = buildUidAndAlias(DSLPlace())
        val (addressUid, a) = buildUidAndAlias(DSLAddress())
        val (placeDescriptorUid, p2) = buildUidAndAlias(DSLPlaceDescriptor())
        val joinTableAlias = AliasBuilder.build("$placeUid$addressUid")

        val expected = "SELECT $p2.description, ${p2}.placerefid, $p.rentInCents, $a.fullAddress" +
                " FROM $placeUid $p" +
                " JOIN $placeUid$addressUid $joinTableAlias ON $joinTableAlias.${placeUid}refid = $p.id" +
                " JOIN $addressUid $a ON ${joinTableAlias}.${addressUid}refid = $a.id" +
                " JOIN $placeDescriptorUid $p2 ON $p.id = $p2.placerefid"

        assertHasContent(candidate)
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testDslSelectWhere() {

        val sql = Sql select DSLPersonB() where DSLPersonB::firstName eq "name"

        val (uid, p) = buildUidAndAlias(DSLPersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'name'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testDslSelectWhereAndWhere() {

        val sql = Sql select DSLPersonB() where DSLPersonB::firstName eq "first" and DSLPersonB::lastName eq "last"

        val (uid, p) = buildUidAndAlias(DSLPersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'first' AND $p.lastName = 'last'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testDslDelete() {

        val uid = UidBuilder.buildUniqueId(DSLPerson())
        val dsl = Sql delete DSLPerson() where DSLPerson::firstName eq "Something"

        val sql = mapper.map(dsl)

        TestUtil.expect("DELETE FROM $uid WHERE firstName = 'Something'", sql)
    }

    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val p = AliasBuilder.build(uid)
        return uid to p
    }
}
