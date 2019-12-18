package mg.util.db

import mg.util.common.Common.nonThrowingBlock
import mg.util.db.AliasBuilder.build
import mg.util.db.DBTest.PersonB
import mg.util.db.UidBuilder.build
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.SqlMapperFactory
import mg.util.db.dsl.mysql.Sql
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

internal class DBOTest {

    private var dbConfig = DBConfig.testConfig

    data class Simple(val ffff: String = "aaaa")
    data class SimpleComp(val gggg: String = "cccc")
    data class Composition(val gggg: String = "bbbb", val hhhh: Simple = Simple("cccc"))
    data class MultipleComposition(val iiii: Int = 0, val hhhh: Simple = Simple("cccc"), val ssss: List<SimpleComp> = listOf(SimpleComp("1111"), SimpleComp("2222")))
    data class Person(val firstName: String = "", val lastName: String = "")
    data class Uuuu(val firstName: String = "", val lastName: String = "")

    // TODO for joined save & find
    data class Billing(val amount: String = "", val person: Person = Person("", ""))

    private val firstName = "firstName"
    private val first1 = "first1"
    private val lastName = "lastName"
    private val last2 = "last2"

    private val testPerson = Person(firstName, lastName)
    private val testPerson2 = Person(first1, last2)

    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testBuildingMetadata() {

        val metadataCandidate: Metadata<Person> = dbo.buildMetadata(testPerson)

        assertNotNull(metadataCandidate)
        assertEquals("Person", metadataCandidate.name)
        assertEquals(2, metadataCandidate.fieldCount)
        assertEquals(Person::class, metadataCandidate.type::class)
        assertNotNull(metadataCandidate.uid)
        assertTrue(metadataCandidate.uid.isNotEmpty())
    }

    @Test
    fun testBuildingUid() {

        val uidCandidate = buildUniqueId(Person(firstName, lastName))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastName).hashCode()}", uidCandidate)
    }

    // @Test // TODO 4: fix ensure for different compositions
    fun testEnsureTable() {
//        val simpleUid = build(Simple::class)
        val multipleCompositionUid = build(MultipleComposition::class)
        val connection = of(dbConfig.connection)

        dbo.ensureTable(MultipleComposition(), dbConfig.connection)
        queryShowTables(connection)
                .any { it.equals(multipleCompositionUid, ignoreCase = true) }
                .apply(::assertTrue)
    }

    // TOIMPROVE: test coverage
    // @Test
    fun testSaveMapAndFind() {

        testSave()

        testMap()

        testFind()
    }


    private fun testMap() {

        val persons: ResultSet? = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { it.executeQuery("SELECT * FROM ${buildUniqueId(testPerson2)}") }
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("Test failed: no test data found") }

        val candidateMapped = ObjectBuilder().buildListOfT(persons, Person())

        assertNotNull(candidateMapped)
        assertTrue(candidateMapped.isNotEmpty())
        assertTrue(candidateMapped.contains(Person("first1", "last2")))
    }

    private fun testFind() {
        val test1 = "test1DBOTest"
        val test2 = "test2DBOTest"

        val person = Person(test1, test2)

        dbo.save(person, dbConfig.connection)

        val personTest2 = Person(test1, test2)

        val candidatePersonList = dbo.find(personTest2, dbConfig.connection)

        assertNotNull(candidatePersonList)
        assertTrue(candidatePersonList.isNotEmpty())
        assertTrue(candidatePersonList.contains(Person(test1, test2)))
    }

    private fun testSave() {

        dbo.ensureTable(testPerson2, dbConfig.connection)
        dbo.save(testPerson2, dbConfig.connection)

        val rs = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { it.executeQuery("SELECT * FROM ${buildUniqueId(testPerson2)}") }
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("Test failed: no rows in db.") }!!

        val candidates = mutableListOf<Person>()
        while (rs.next()) {
            candidates += Person(rs.getString("firstName"), rs.getString("lastName"))
        }

        assertNotNull(candidates)
        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.contains(testPerson2))
    }

    // @Test
    fun testSaveWithComposition() {

        // TODO 7: use composition for testing

        val b = buildUniqueId(Billing())
        val p = buildUniqueId(Person())
        val b2 = build(b)
        val p2 = build(p)

        val sql = "SELECT * FROM $p $p2 JOIN $b $b2 ON $p2.id = $b2.${p}id"

        dbo.ensureTable(Billing(), dbConfig.connection)
        // dbo.save(Billing("10", Person("fff", "lll")), dbConfig.connection)

        val candidate = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { it.executeQuery(sql) }
//                .map { it.metaData }
//                .map(::getColumnsAsCSVdata)
//                .ifPresent (::println)

        assertTrue(candidate.get()!!.next())
    }

//    private fun getColumnsAsCSVdata(metaData: ResultSetMetaData?): String {
//
//        return of(metaData)
//                .map {
//                    (1..it.columnCount).joinToString(", ") { i ->
//                        it.getColumnName(i)
//                    }
//                }
//                .getOrElse("")
//    }

    @Test
    fun testDrop() {

        data class Tttt(val value: String = "")

        val uid = buildUniqueId(Tttt())
        val connection = of(dbConfig.connection)

        connection.map(Connection::createStatement)
                .mapWith(uid) { s, u -> s.executeUpdate("CREATE TABLE IF NOT EXISTS $u(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, value VARCHAR(64) NOT NULL)") }

        queryShowTables(connection)
                .any { it.equals(uid, ignoreCase = true) }
                .apply(::assertTrue)

        dbo.drop(Tttt(), dbConfig.connection)

        queryShowTables(connection)
                .none { it.equals(uid, ignoreCase = true) }
                .apply(::assertTrue)
    }

    private fun queryShowTables(connection: Opt2<Connection>): List<String> {
        return connection.map(Connection::createStatement)
                .map { it.executeQuery("SHOW TABLES") }
                .getAndMap(::iof)!!
                .map { it.getString(1) }
    }

    @Test
    fun testDslSelect2() {

        val name = "name"
        val bbb = "bbb"

        dbo.ensureTable(Uuuu(), dbConfig.connection)
        dbo.save(Uuuu(name, bbb), dbConfig.connection)

        val sql = Sql() select Uuuu() where Uuuu::firstName eq name and Uuuu::lastName eq bbb

        val list = dbo.findBy(sql, dbConfig.connection)

        assertTrue(list.isNotEmpty())
        assertTrue(list.contains(Uuuu(name, bbb)))
    }

    @Suppress("unused")
    companion object {

        @AfterAll
        @JvmStatic
        internal fun afterAll() {

            val dbConfig = DBConfig.testConfig

            val tableUids = listOf(Simple(),
                    Composition(),
                    MultipleComposition(),
                    // Person(), shared class between tests
                    // PersonB(), shared class between tests
                    Uuuu(),
                    Billing())
                    .map(::buildUniqueId)

            forStatement(dbConfig, tableUids) { stmt, uid ->
                deleteFromUid(stmt, uid)
                dropTableUid(stmt, uid)
            }
        }

        private fun forStatement(dbConfig: DBConfig, uids: List<String>, mapper: (Statement, String) -> Unit) {
            of(dbConfig.connection)
                    .map(Connection::createStatement)
                    .ifPresent { stmt -> uids.forEach { uid -> mapper(stmt, uid) } }
        }

        private fun deleteFromUid(s: Statement, uid: String) = nonThrowingBlock { s.executeUpdate("DELETE FROM $uid") }
        private fun dropTableUid(s: Statement, uid: String) = nonThrowingBlock { s.executeUpdate("DROP TABLE IF EXISTS $uid") }
    }
}

