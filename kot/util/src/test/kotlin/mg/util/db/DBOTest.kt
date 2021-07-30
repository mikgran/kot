package mg.util.db

import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder.build
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.db.dsl.DslMapperFactory
import mg.util.db.dsl.Sql
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

// TODO 1 Tests are currently order dependent, fix all tests so that none fail from a change in testing framework
internal class DBOTest {

    private var dbConfig = DBConfig(TestConfig())

    private val firstName = "firstName"
    private val first1 = "first1"
    private val lastName = "lastName"
    private val last2 = "last2"

    private val testPerson = DBOPerson(firstName, lastName)
    private val testPerson2 = DBOPerson(first1, last2)

    private val dbo = DBO(DefaultDslMapper("mysql"))

    @Test
    fun testBuildingMetadata() {

        val metadataCandidate = dbo.buildMetadata(testPerson)

        assertNotNull(metadataCandidate)
        assertEquals("DBOPerson", metadataCandidate.name)
        assertEquals(2, metadataCandidate.fieldCount)
        assertEquals(DBOPerson::class, metadataCandidate.type::class)
        assertNotNull(metadataCandidate.uid)
        assertTrue(metadataCandidate.uid.isNotEmpty())
    }

    @Test
    fun testBuildingUid() {

        val uidCandidate = buildUniqueId(DBOPerson(firstName, lastName))

        assertNotNull(uidCandidate)
        assertEquals("DBOPerson${(firstName + lastName).hashCode()}", uidCandidate)
    }

    @Test
    fun testEnsureTable() {
        val multipleCompositionUid = build(DBOMultipleComposition::class)
        val connection = of(dbConfig.connection)

        dbo.ensureTable(DBOMultipleComposition(), dbConfig.connection)
        queryShowTables(connection)
                .any { it.equals(multipleCompositionUid, ignoreCase = true) }
                .apply(::assertTrue)
    }

    // TOIMPROVE: test coverage
    @Test
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

        val candidateMapped = ObjectBuilder().buildListOfT(persons, DBOPerson())

        assertNotNull(candidateMapped)
        assertTrue(candidateMapped.isNotEmpty())
        assertTrue(candidateMapped.contains(DBOPerson("first1", "last2")))
    }

    private fun testFind() {
        val test1 = "test1DBOTest"
        val test2 = "test2DBOTest"

        val dboPerson = DBOPerson(test1, test2)

        dbo.save(dboPerson, dbConfig.connection)

        val dboPerson2 = DBOPerson(test1, test2)

        val candidatePersonList = dbo.find(dboPerson2, dbConfig.connection)

        assertNotNull(candidatePersonList)
        assertTrue(candidatePersonList.isNotEmpty())
        assertTrue(candidatePersonList.contains(DBOPerson(test1, test2)))
    }

    private fun testSave() {

        DB().save(testPerson2)

        dbo.ensureTable(testPerson2, dbConfig.connection)
        dbo.save(testPerson2, dbConfig.connection)

        val rs = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { it.executeQuery("SELECT * FROM ${buildUniqueId(testPerson2)}") }
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("Test failed: no rows in db.") }!!

        val candidates = mutableListOf<DBOPerson>()
        while (rs.next()) {
            candidates += DBOPerson(rs.getString("firstName"), rs.getString("lastName"))
        }

        assertNotNull(candidates)
        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.contains(testPerson2))
    }

    // XXX: 110 fix this, asserts
    @Test
    fun testSaveWithComposition() {

        // TODO 1: use composition for testing
        // - test one to one, one to many

        val connection = dbConfig.connection
        dbo.ensureTable(DBOBilling2(), connection)
        val dboBilling2 = DBOBilling2("10", listOf(DBOPerson3("A1", "AA1"), DBOPerson3("B1", "BB1"), DBOPerson3("C1", "CC1")))
        dbo.save(dboBilling2, connection)

        val sql = Sql select DBOBilling2() where DBOBilling2::amount eq 10//  and DBOPerson3::firstName eq "Firstname" and DBOPerson3::lastName eq "Lastname"
        sql.parameters().isPrimaryIdIncluded = true

        val sqlStr = DslMapperFactory.get().map(sql)// .also { println(it) }

//        println()
//        println("dboBilling2: $dboBilling2")
//        println()
//        println("sqlStr: $sqlStr")
//        println()

        val results: Opt2<ResultSet> = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { it.executeQuery(sqlStr) }

        // FIXME: 200 asserts

        // XXX: 500 Fix composition building
        val dboBillingCandidate: MutableList<DBOBilling2> =
                ObjectBuilder()
                        .buildListOfT(results.get(), DBOBilling2())

        println("\ndboBillingCandidate: $dboBillingCandidate")
        // fail("")
    }

    @Test
    fun testShowColumns() {

        dbo.ensureTable(DBOPerson3(), dbConfig.connection)
        val candidate: List<String> = dbo.showColumns(DBOPerson3(), dbConfig.connection)

        assertNotNull(candidate)
        assertTrue(candidate.isNotEmpty())
        assertTrue(candidate.contains("firstName"))
        assertTrue(candidate.contains("lastName"))
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

        data class DBOTttt(val value: String = "")

        val uid = buildUniqueId(DBOTttt())
        val connection = of(dbConfig.connection)

        connection.map(Connection::createStatement)
                .mapWith(uid) { s, u -> s.executeUpdate("CREATE TABLE IF NOT EXISTS $u(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, value VARCHAR(64) NOT NULL)") }

        queryShowTables(connection)
                .any { it.equals(uid, ignoreCase = true) }
                .apply(::assertTrue)

        dbo.drop(DBOTttt(), dbConfig.connection)

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

    @Suppress("unused")
    companion object {

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            listOf(
                    DBOSimple(),
                    DBOComposition(),
                    DBOMultipleComposition(),
                    DBOSimpleComp(),
                    DBOMultipleComposition(),
                    DBOPerson3(),
                    DBOPerson2(),
                    DBOPerson(),
                    DBOBilling(),
                    DBOBilling2()
            ).also { TestSupport.dropTables(it) }

            listOf(
                    Pair(DBOBilling2(), DBOPerson3()),
                    Pair(DBOMultipleComposition(), DBOSimple()),
                    Pair(DBOMultipleComposition(), DBOSimpleComp())
            ).also { TestSupport.dropJoinTables(it) }
        }
    }
}

