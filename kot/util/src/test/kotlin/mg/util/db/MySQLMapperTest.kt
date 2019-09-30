package mg.util.db

import mg.util.db.DBOTest.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MySQLMapperTest {

    private val person = Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testCreateTable() {

        val personMetadata = dbo.buildMetadata(person)

        val createTableSqlCandidate = MySQLMapper.buildCreateTable(personMetadata)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE IF NOT EXISTS ${personMetadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

        // TOIMPROVE: test coverage for exceptions
    }

    @Test
    fun testBuildSqlFieldDefinitionsForMetadata() {

        val personMetadata = dbo.buildMetadata(person)

        val fieldDefinitionsCandidate = MySQLMapper.buildSqlFieldDefinitions(personMetadata)

        val expectedFieldDefinitions = listOf("firstName VARCHAR(64) NOT NULL", "lastName VARCHAR(64) NOT NULL")

        assertNotNull(fieldDefinitionsCandidate)
        assertTrue(fieldDefinitionsCandidate.containsAll(expectedFieldDefinitions))
    }

    @Test
    fun testBuildInsert() {

        val personMetadata = dbo.buildMetadata(person)

        val insertCandidate = MySQLMapper.buildInsert(personMetadata)

        val expectedInsert = "INSERT INTO ${personMetadata.uid} (firstName, lastName) VALUES ('testname1', 'testname2')"

        assertNotNull(insertCandidate)
        assertEquals(expectedInsert, insertCandidate)
    }

    @Test
    fun testFinding() {

        val personMetadata = dbo.buildMetadata(person)

        val findCandidate = MySQLMapper.buildFind(personMetadata)

        val expectedFind = "SELECT * FROM ${personMetadata.uid}"

        assertNotNull(findCandidate)
        assertEquals(expectedFind, findCandidate)
    }
}
