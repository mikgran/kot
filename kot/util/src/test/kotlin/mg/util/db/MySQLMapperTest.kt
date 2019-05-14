package mg.util.db

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

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

        val personMeta = dbo.buildMetadata(person)

        val createTableSqlCandidate = MySQLMapper.buildCreateTable(personMeta)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE ${personMeta.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

        // TOIMPROVE: test coverage for exceptions
    }

    @Test
    fun testBuildTypeDefsForMetadata() {

        val metadata = dbo.buildMetadata(person)

        val typeDefsCandidate = MySQLMapper.buildSqlFieldDefs(metadata)

        val expectedTypeDefs = listOf("firstName VARCHAR(64) NOT NULL", "lastName VARCHAR(64) NOT NULL")

        assertNotNull(typeDefsCandidate)
        assertTrue(typeDefsCandidate.containsAll(expectedTypeDefs))
    }
}