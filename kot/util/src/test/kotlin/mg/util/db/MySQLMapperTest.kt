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
        assertEquals("CREATE TABLE PERSONS(id MEDIUMINT NOT NULL AUTO_INCREMENT, firstname VARCHAR(64) NOT NULL, lastname VARCHAR(64) NOT NULL)", createTableSqlCandidate)

    }
}