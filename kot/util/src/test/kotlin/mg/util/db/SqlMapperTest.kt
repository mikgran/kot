package mg.util.db

import mg.util.db.DBOTest.Person
import mg.util.db.dsl.mysql.Sql
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SqlMapperTest {

    private val person = Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.get("mysql"))
    private val personUid = UidBuilder.build(Person::class)
    private val personAlias = AliasBuilder.alias(personUid)

    @Test
    fun testCreateTable() {

        val personMetadata = dbo.buildMetadata(person)

        val createTableSqlCandidate = SqlMapper(Sql()).buildCreateTable(personMetadata)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE IF NOT EXISTS ${personMetadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

        // TOIMPROVE: test coverage for exceptions
    }

    @Test
    fun testBuildInsert() {

        val personMetadata = dbo.buildMetadata(person)

        val insertCandidate = SqlMapper(Sql()).buildInsert(personMetadata)

        val expectedInsert = "INSERT INTO ${personMetadata.uid} (firstName, lastName) VALUES ('testname1', 'testname2')"

        assertNotNull(insertCandidate)
        assertEquals(expectedInsert, insertCandidate)
    }

    @Test
    fun testFinding() {

        val personMetadata = dbo.buildMetadata(person)

        val findCandidate = SqlMapper(Sql()).buildFind(personMetadata)

        val expectedFind = "SELECT $personAlias.firstName, $personAlias.lastName FROM $personUid $personAlias"

        assertNotNull(findCandidate)
        assertEquals(expectedFind, findCandidate)
    }
}
