package mg.util.db.dsl

import mg.util.common.Common
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

// FIXME: 50 Fix all select, select-join, multitable inserts, creates
// TOIMPROVE: test coverage
internal class DslMapperTest {

    private val mapper = DslMapperFactory.get()

    private fun <T : Any> expect(expected: T, candidate: T) {
        assertNotNull(candidate)
        if (expected != candidate) {
            println("\nE:\n<$expected>")
            println("C:\n<$candidate>")
            if (expected is String && candidate is String) {
                val common = longestCommonSubstrings(expected, candidate)
                if (common.size > 0) {
                    println("\nLongest common part:\n<${common.first()}>")
                }
            }
        }
        assertEquals(expected, candidate)
    }

    // ref: https://en.wikipedia.org/wiki/Longest_common_substring_problem
    private fun longestCommonSubstrings(s: String, t: String): MutableSet<String> {
        val table = Array(s.length) { IntArray(t.length) }
        var longest = 0
        val result: MutableSet<String> = HashSet()
        for (i in s.indices) {
            for (j in t.indices) {
                if (s[i] != t[j]) {
                    continue
                }
                table[i][j] = if (i == 0 || j == 0) 1 else 1 + table[i - 1][j - 1]
                if (table[i][j] > longest) {
                    longest = table[i][j]
                    result.clear()
                }
                if (table[i][j] == longest) {
                    result.add(s.substring(i - longest + 1, i + 1))
                }
            }
        }
        return result
    }

    private fun assertHasContent(candidate: String) {
        assertTrue(Common.hasContent(candidate), "no mapped content")
    }

    @Test
    fun testCreatingANewTable() {

        val sql = Sql create DSLPersonB()
        val uid = UidBuilder.build(DSLPersonB::class)
        val candidate = mapper.map(sql)

        val expected = "CREATE TABLE IF NOT EXISTS $uid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)"
        expect(expected, candidate)
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

        expect(expected, candidate)
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

        expect(expected, candidate)
    }

    // FIXME 98: Insert-No-Custom-Object-Relations
    @Test
    fun testInsertNoCustomRelations() {

        val sql = Sql insert DSLPerson("first", "last")

        val (personUid, p) = buildUidAndAlias(DSLPerson())

        val expected = "INSERT INTO $personUid $p VALUES(10)" +
                ";" +
                ""


    }

    // FIXME: 99
    @Test
    fun testInsertOneToManyRelation() {


    }

    // FIXME: 100 Insert and multi level insert?
    // limit to just one layer?
    @Test
    fun testInsertOneToOneRelation() {

        val dslAddress2 = DSLAddress2("anAddress")
        val dslPlace2 = DSLPlace2(dslAddress2, rentInCents = 80000)

        val dslAddress2Uid = UidBuilder.buildUniqueId(dslAddress2)
        val dslPlace2Uid = UidBuilder.buildUniqueId(dslPlace2)

        val sql = Sql insert dslPlace2

        val candidate: String = mapper.map(sql)

        val expected = "INSERT INTO $dslPlace2Uid (rentInCents) VALUES ('80000');" +
                "INSERT INTO $dslAddress2Uid (fullAddress,${dslPlace2Uid}refid) VALUES ('anAddress'," +
                "(SELECT id from $dslPlace2Uid WHERE rentInCents='80000'))"

        expect(expected, candidate)
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

        expect(expected, candidate)
    }

    // FIXME: 101 move auto refs to a join-table car, window, jointable: carwindow
    @Test
    fun testBuildingSqlFromDslJoin_AutoRef() {

        // SELECT p.address, p.rentInCents, a.fullAddress
        // FROM
        //      Place1234556 p
        // JOIN
        //      Place123456Address123456 p2 ON p2.place123456refid = p.id
        // JOIN
        //      Address123565 a ON a.id = p2.address123456refid

        val dsl = Sql select DSLPlace()
        val candidate = mapper.map(dsl)

        val (placeUid, p) = buildUidAndAlias(DSLPlace())
        val (addressUid, a) = buildUidAndAlias(DSLAddress())
        val joinTableAlias = AliasBuilder.build("$placeUid$addressUid")

        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $placeUid $p" +
                " JOIN $placeUid$addressUid $joinTableAlias ON $joinTableAlias.${placeUid}refid = $p.id" +
                " JOIN $addressUid $a ON ${joinTableAlias}.${addressUid}refid = $a.id"

        assertHasContent(candidate)
        expect(expected, candidate)
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

        val dsl = Sql select DSLPlace() join DSLPlaceDescriptor() on DSLPlace::class eq DSLPlaceDescriptor::placeRefId
        val candidate = mapper.map(dsl)

        val (uidPlace, p) = buildUidAndAlias(DSLPlace())
        val (uidAddress, a) = buildUidAndAlias(DSLAddress())
        val (uidDesc, p2) = buildUidAndAlias(DSLPlaceDescriptor())

        val expected = "SELECT $p2.description, ${p2}.placerefid, $p.address, $p.rentInCents, $a.fullAddress" +
                " FROM $uidPlace $p" +
                " JOIN $uidDesc $p2 ON $p.id = $p2.placerefid" +
                " JOIN $uidAddress $a ON $p.id = $a.${uidPlace}refid"

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    @Test
    fun testDslSelectWhere() {

        val sql = Sql select DSLPersonB() where DSLPersonB::firstName eq "name"

        val (uid, p) = buildUidAndAlias(DSLPersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'name'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    @Test
    fun testDslSelectWhereAndWhere() {

        val sql = Sql select DSLPersonB() where DSLPersonB::firstName eq "first" and DSLPersonB::lastName eq "last"

        val (uid, p) = buildUidAndAlias(DSLPersonB())
        val expected = "SELECT $p.firstName, $p.lastName FROM $uid $p WHERE $p.firstName = 'first' AND $p.lastName = 'last'"
        val candidate = mapper.map(sql)

        assertHasContent(candidate)
        expect(expected, candidate)
    }

    @Test
    fun testDslDelete() {

        val uid = UidBuilder.buildUniqueId(DSLPerson())
        val dsl = Sql delete DSLPerson() where DSLPerson::firstName eq "Something"

        val sql = mapper.map(dsl)

        expect("DELETE FROM $uid WHERE firstName = 'Something'", sql)
    }

    private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
        val uid = UidBuilder.buildUniqueId(t)
        val p = AliasBuilder.build(uid)
        return uid to p
    }
}
