package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MySQLMapperTest {

    private val person = DBOTest.Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.getDefault())

    @Test
    fun find() {
    }

    @Test
    fun insert() {
    }

    @Test
    fun testCreateTable() {

        val personMetadata = dbo.buildMetadata(person)

        val createTableSqlCandidate = MySQLMapper.buildCreateTable(personMetadata)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE ${personMetadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

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

        val expectedFind = "SELECT firstName, lastName FROM ${personMetadata.uid}"

        assertNotNull(findCandidate)
        assertEquals(expectedFind, findCandidate)
    }



}
