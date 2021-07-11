package mg.util.db.dsl

import mg.util.common.Common
import mg.util.common.TestUtil
import mg.util.db.AliasBuilder
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import mg.util.db.dsl.DslMapperTest.SqlBuilder.Companion.createJoinTable
import mg.util.db.dsl.DslMapperTest.SqlBuilder.Companion.createTable
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

        val expected =
                createTable(uid, "") {
                    varChar64("firstName")
                    varChar64("lastName")
                }

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToManyRelation() {

        val sql = Sql create DSLBuilding("some address", listOf(DSLFloor(1)))
        val buildingUid = UidBuilder.build(DSLBuilding::class)
        val floorUid = UidBuilder.build(DSLFloor::class)

        val candidate = mapper.map(sql)

        val expected = "" +
                createTable(buildingUid) { varChar64("fullAddress") } +
                createTable(floorUid) { mediumInt("number") } +
                createJoinTable(buildingUid, floorUid, "")

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToManyMultipleDepthRelation() {

        val dslAddressM1 = DSLAddressM1(10, DSLLocationM1("TTTT", listOf(DSLPersonM1("VVVV"))))
        val sql = Sql create dslAddressM1

        val addressM1Uid = UidBuilder.build(DSLAddressM1::class)
        val locationM1Uid = UidBuilder.build(DSLLocationM1::class)
        val personM1Uid = UidBuilder.build(DSLPersonM1::class)

        val expected = "" +
                createTable(addressM1Uid) { mediumInt("rent") } +
                createTable(locationM1Uid) { varChar64("loc") } +
                createJoinTable(addressM1Uid, locationM1Uid) +
                createTable(personM1Uid) { varChar64("name") } +
                createJoinTable(locationM1Uid, personM1Uid, "")

        val candidate = mapper.map(sql)

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testCreatingANewTableWithOneToOneRelation() {

        val sql = Sql create DSLPlace(DSLAddress("somePlace"), 100000)
        val placeUid = UidBuilder.build(DSLPlace::class)
        val addressUid = UidBuilder.build(DSLAddress::class)

        val candidate = mapper.map(sql)

        val expected = "" +
                createTable(placeUid) { mediumInt("rentInCents") } +
                createTable(addressUid) { varChar64("fullAddress") } +
                createJoinTable(placeUid, addressUid, "")

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testInsertNoCustomRelations() {

        val sql = Sql insert DSLPerson("first", "last")

        val (personUid, _) = buildUidAndAlias(DSLPerson())

        val expected = buildInsertInto(personUid, l("firstName", "lastName"), l("first", "last"), "")

        val candidate = mapper.map(sql)

        TestUtil.expect(expected, candidate)
    }

    @Test
    fun testInsertOneToOneRelation() {

        val dslAddress2 = DSLAddress2("anAddress")
        val dslPlace2 = DSLPlace2(dslAddress2, rentInCents = 80000)

        val dslPlace2Uid = UidBuilder.buildUniqueId(dslPlace2)
        val dslAddress2Uid = UidBuilder.buildUniqueId(dslAddress2)

        val sql = Sql insert dslPlace2

        val candidate: String = mapper.map(sql)

        val expected = "" +
                buildInsertInto(dslPlace2Uid, l("rentInCents"), l("80000")) +
                buildSelectLastInsertId(PARENT_LAST_ID) +
                buildInsertInto(dslAddress2Uid, l("fullAddress"), l("anAddress")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace2Uid, dslAddress2Uid, "")

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

        val sql = Sql insert dslPlace3
        val candidate: String = mapper.map(sql)

        val expected = "" +
                buildInsertInto(dslPlace3Uid, l("rentInCents"), l("80000")) +
                buildSelectLastInsertId(PARENT_LAST_ID) +

                buildInsertInto(dslAddress3Uid, l("fullAddress"), l("anAddress")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace3Uid, dslAddress3Uid) +

                buildInsertInto(dslFloor3Uid, l("number"), l("10")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace3Uid, dslFloor3Uid) +

                buildInsertInto(dslFloor3Uid, l("number"), l("20")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace3Uid, dslFloor3Uid, "")

        TestUtil.expect(expected, candidate)
    }

    // @Test // FIXME 10000
    fun testInsertOneToManyMultiDepth() {

        val dslAddress4 = DSLAddress4("AAAA")
        val dslPerson4 = DSLPerson4("FFFF", "LLLL", dslAddress4)
        val dslFloors4 = listOf(DSLFloor4(1), DSLFloor4(2), DSLFloor4(3))
        val dslPlace4 = DSLPlace4(dslPerson4, dslFloors4, rentInCents = 50000)

        val dslPerson4Uid = UidBuilder.buildUniqueId(dslPerson4)
        val dslAddress4Uid = UidBuilder.buildUniqueId(dslAddress4)
        val dslPlace4Uid = UidBuilder.buildUniqueId(dslPlace4)
        val dslFloor4Uid = UidBuilder.buildUniqueId(DSLFloor4())

        val sql = Sql insert dslPlace4
        val candidate: String = mapper.map(sql)

        val expected = "" +
                buildInsertInto(dslPlace4Uid, l("rentInCents"), l("50000")) +
                buildSelectLastInsertId(PARENT_LAST_ID) +

                buildInsertInto(dslPerson4Uid, l("firstName", "lastName"), l("FFFF", "LLLL")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace4Uid, dslPerson4Uid) +

                buildInsertInto(dslFloor4Uid, l("number"), l("1")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace4Uid, dslFloor4Uid) +

                buildInsertInto(dslFloor4Uid, l("number"), l("2")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace4Uid, dslFloor4Uid) +

                buildInsertInto(dslFloor4Uid, l("number"), l("3")) +
                buildSelectLastInsertId(CHILD_LAST_ID) +
                buildInsertJoinForParentAndChild(dslPlace4Uid, dslFloor4Uid) +

                buildInsertInto(dslAddress4Uid, l("fullAddress"), l("AAAA"))
                buildSelectLastInsertId(CHILD_LAST_ID)
        // when creating a new child, add a new childNumberLastId


        TestUtil.expect(expected, candidate)
    }

    private fun buildSelectLastInsertId(lastId: String, trail: String = ";"): String =
            "SELECT LAST_INSERT_ID() INTO @$lastId$trail"

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

    private fun buildInsertInto(tableName: String, cols: List<String>, values: List<String>, trail: String = ";"): String {
        val colsSql = cols.joinToString(", ") { it }
        val valuesSql = values.joinToString(", ") { "'$it'" }

        return "INSERT INTO $tableName ($colsSql) VALUES ($valuesSql)$trail"
    }

    private fun buildInsertJoinForParentAndChild(parentUid: String, childUid: String, trail: String = ";"): String =
            "INSERT INTO $parentUid$childUid (${parentUid}refid, ${childUid}refid) VALUES (@${PARENT_LAST_ID}, @${CHILD_LAST_ID})$trail"

    // for brevity with reading tests
    private fun <T : Any> l(vararg any: T): List<T> {
        return listOf(*any)
    }

    companion object {
        private const val PARENT_LAST_ID = "parentLastId"
        private const val CHILD_LAST_ID = "childLastId"
    }

    private class SqlBuilder {

        private val tableSqls = mutableListOf<String>()
        private val columnSqls = mutableListOf<String>()

        fun String.addTableSql() = tableSqls.add(this)
        fun String.addColumnSql() = columnSqls.add(this)

        fun createTable(str: String) = this.also { "CREATE TABLE IF NOT EXISTS $str".addTableSql() }
        fun id() = this.also { "id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY".addColumnSql() }
        fun varChar64(columnName: String) = this.also { "$columnName VARCHAR(64) NOT NULL".addColumnSql() }
        fun build() = tableSqls.joinToString(" ") + "(${columnSqls.joinToString(", ")})"
        fun buildWithSemiColon() = build() + ";"
        fun mediumInt(str: String) = this.also { "$str MEDIUMINT NOT NULL".addColumnSql() }

        companion object {
            fun createJoinTable(table1: String, table2: String, trail: String = ";"): String {
                return SqlBuilder()
                        .createTable(table1 + table2)
                        .id()
                        .mediumInt(table1.refidPostFix())
                        .mediumInt(table2.refidPostFix())
                        .build() + trail
            }

            fun createTable(table1: String, trail: String = ";", fieldSupplier: SqlBuilder.() -> Unit): String {
                val builder = SqlBuilder()
                        .createTable(table1)
                        .id()
                fieldSupplier(builder)
                return builder.build() + trail
            }
        }
    }
}

fun String.refidPostFix() = this + "refid"