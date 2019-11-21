package mg.util.db

import mg.util.common.Common.nonThrowingBlock
import mg.util.db.DBTest.*
import mg.util.db.dsl.mysql.Sql
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

internal class DBOTest {

    private var dbConfig: DBConfig = DBConfig(TestConfig())

    data class Person(val firstName: String = "", val lastName: String = "")
    data class Uuuu(val firstName: String = "", val lastName: String = "")

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

        val uidCandidate = dbo.buildUniqueId(Person(firstName, lastName))

        assertNotNull(uidCandidate)
        assertEquals("Person${(firstName + lastName).hashCode()}", uidCandidate)
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
                .map { s -> s.executeQuery("SELECT * FROM ${dbo.buildMetadata(testPerson2).uid}") }
                .filter(ResultSet::next)
                .getOrElseThrow { Exception("Test failed: no test data found") }

        val candidateMapped = ObjectBuilder().buildListOfT(persons, Person())

        assertNotNull(candidateMapped)
        assertTrue(candidateMapped.isNotEmpty())
        assertTrue(contains("first1", "last2", candidateMapped))
    }

    private fun contains(firstName: String, lastName: String, candidateMapped: List<Person>): Boolean {
        var found = false
        candidateMapped.forEach {
            if (firstName == it.firstName && lastName == it.lastName) {
                found = true
            }
        }
        return found
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
        assertTrue(contains(test1, test2, candidatePersonList))
    }

    private fun testSave() {

        dbo.ensureTable(testPerson2, dbConfig.connection)

        dbo.save(testPerson2, dbConfig.connection)

        val candidate = of(dbConfig.connection)
                .map(Connection::createStatement)
                .map { s -> s.executeQuery("SELECT * FROM ${dbo.buildMetadata(testPerson2).uid}") }
                .filter(ResultSet::next)
                .map { rs -> "${rs.getString("firstName")} ${rs.getString("lastName")}" }
                .getOrElseThrow { Exception("Test failed: no test data found") }

        assertNotNull(candidate)
        assertEquals("$first1 $last2", candidate)
    }

    @Test
    fun testDslSelect() {

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

            val person = Person("", "")
            val personB = PersonB("", "")
            val uuuu = Uuuu("", "")
            val dbConfig = DBConfig(TestConfig())
            val dbo = DBO(SqlMapperFactory.get("mysql"))
            val personUid = dbo.buildUniqueId(person)
            val personBUId = dbo.buildUniqueId(personB)
            val uuuuUid = dbo.buildUniqueId(uuuu)
            val list = listOf(personUid, personBUId, uuuuUid)

            of(dbConfig.connection)
                    .map(Connection::createStatement)
                    .ifPresent { stmt -> list.forEach { uid -> deleteFromUid(stmt, uid) } }
                    .ifPresent { stmt -> list.forEach { uid -> dropTableUid(stmt, uid) } }
        }

        private fun dropTableUid(statement: Statement, uid: String) {
            nonThrowingBlock { statement.executeUpdate("DROP TABLE $uid") }
        }

        private fun deleteFromUid(statement: Statement, uid: String) {
            nonThrowingBlock { statement.executeUpdate("DELETE FROM $uid") }
        }
    }
}

