package mg.util.db

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled

internal class MySQLMapperTest {

    val person = DBOTest.Person("testname1", "testname2")
    val dbo = DBO()

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
    @Disabled
    fun testBuildInsert() {

        val personMetadata = dbo.buildMetadata(person)

        val insertCandidate = MySQLMapper.buildInsert(personMetadata)

        val expectedInsert = "INSERT INTO ${personMetadata.uid} (firstName, lastName) VALUES ('testname1', 'testname2')"

        assertNotNull(insertCandidate)
        assertEquals(expectedInsert, insertCandidate)
    }


}
