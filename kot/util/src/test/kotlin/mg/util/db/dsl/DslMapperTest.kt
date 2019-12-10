package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.common.FunctionComposition.Companion.plus
import mg.util.common.PredicateComposition.Companion.and
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.PredicateComposition.Companion.rangeTo
import mg.util.common.PredicateComposition.Companion.not
import mg.util.db.AliasBuilder
import mg.util.db.DBTest.PersonB
import mg.util.db.UidBuilder
import mg.util.db.UidBuilder.buildUniqueId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import mg.util.db.dsl.mysql.Sql as MySql
import mg.util.db.dsl.oracle.Sql as SqlOracle

internal class DslMapperTest {

    private data class Address(var fullAddress: String = "")
    private data class Place(var address: Address = Address(), var rentInCents: Int = 0)
    private data class Floor(var number: Int = 0)
    private data class Building(var fullAddress: String = "", var address: Address = Address(), var floors: List<Floor> = listOf(Floor(1)))

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

    // FIXME 6 multiple table creation
    @Test
    fun testCreatingANewTableWithSimpleReference() {

        val sql = MySql() create Building("some address")

        val buildingUid = UidBuilder.build(Building::class)
        val floorUid = UidBuilder.build(Floor::class)

        val candidate = DslMapper.map(sql.list())

        println(candidate)

        assertNotNull(candidate)
//        assertEquals("CREATE TABLE IF NOT EXISTS $buildingUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
//                "fullAddress VARCHAR(64) NOT NULL);" +
//                "CREATE TABLE IF NOT EXISTS $floorUid(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
//                "number VARCHAR(64) NOT NULL);" +
//                "ALTER TABLE $floorUid ADD CONSTRAINT FOREIGN KEY (${buildingUid}id) REFERENCES $buildingUid (id);",
//                candidate)
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

        // FIXME 1: "on a.f = b.f2", needs to be completed

        val sql = MySql() select Place() join Address()

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

    @Test
    fun test_FunctionAndPredicateComposition() {

        fun same(value: Int): Int = value
        fun twice(value: Int): Int = value * 2
        fun thrice(value: Int): Int = value * 3

        fun isLengthLessThanTen(s: String): Boolean = s.length < 10
        fun isAContained(s: String): Boolean = s.contains("a")

        fun isLengthLessThanTenOrIsAContained(s: String) = (::isLengthLessThanTen or ::isAContained)(s)
        assertFalse(isLengthLessThanTenOrIsAContained("bbbbbbbbbb"))
        assertTrue(isLengthLessThanTenOrIsAContained("bbbbbbbbba"))
        assertTrue(isLengthLessThanTenOrIsAContained("bbbbbbb"))

        fun isAContainedInLengthLessThanTen(s: String) = (::isLengthLessThanTen..::isAContained)(s)
        fun isAContainedInLengthLessThanTenB(s: String) = (::isLengthLessThanTen and ::isAContained)(s)
        assertTrue(isAContainedInLengthLessThanTen("a"))
        assertFalse(isAContainedInLengthLessThanTen("abccbbccbb"))
        assertFalse(isAContainedInLengthLessThanTenB("abccbbccbb"))

        fun multiplyBy6(i: Int) = (::same + ::twice + ::thrice)(i)
        fun multiplyBy2(i: Int) = (::same + ::twice)(i)
        assertEquals(6, multiplyBy6(1))
        assertEquals(2, multiplyBy2(1))

        fun isNotAContained(s: String) = (!::isAContained)(s)
        assertTrue(isNotAContained("bbb"))
        assertFalse(isNotAContained("aaa"))
    }

}
