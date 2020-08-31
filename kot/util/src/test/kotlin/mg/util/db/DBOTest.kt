package mg.util.db

import mg.util.db.AliasBuilder.build
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder.build
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
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

    // @Test // XXX: 10 fix this
    fun testSaveWithComposition() {

        // TODO 1: use composition for testing

        val b = buildUniqueId(DBOBilling())
        val p = buildUniqueId(DBOPerson())
        val b2 = build(b)
        val p2 = build(p)

        val sql = "SELECT * FROM $p $p2 JOIN $b $b2 ON $p2.id = $b2.${p}refid"

        // println("sql: $sql")

        dbo.ensureTable(DBOBilling(), dbConfig.connection)
        dbo.save(DBOBilling("10", DBOPerson("fff", "lll")), dbConfig.connection)

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
                    DBOPerson2(),
                    DBOPerson()
                    // , DBOBilling()
            )
                    .also { TestSupport.dropTables(it) }
        }
    }
}

