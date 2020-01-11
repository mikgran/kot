package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.DBO
import mg.util.db.TestDataClasses.Person
import mg.util.db.UidBuilder
import mg.util.db.dsl.mysql.Sql
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SqlMapperTest {

    private val person = Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.get("mysql"))
    private val personUid = UidBuilder.build(Person::class)
    private val personAlias = AliasBuilder.build(personUid)
    private val personMetadata = dbo.buildMetadata(person)

    @Test
    fun testCreateTable() {

        val createTableSqlCandidate = SqlMapper(Sql(), MYSQL).buildCreateTable(personMetadata)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE IF NOT EXISTS ${personMetadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

        // TOIMPROVE: test coverage for exceptions
    }

    @Test
    fun testBuildInsert() {

        val insertCandidate = SqlMapper(Sql(), MYSQL).buildInsert(personMetadata)

        val expectedInsert = "INSERT INTO ${personMetadata.uid} (firstName, lastName) VALUES ('testname1', 'testname2')"

        assertNotNull(insertCandidate)
        assertEquals(expectedInsert, insertCandidate)
    }

    @Test
    fun testFinding() {

        val findCandidate = SqlMapper(Sql(), MYSQL).buildFind(personMetadata)

        val expectedFind = "SELECT $personAlias.firstName, $personAlias.lastName FROM $personUid $personAlias"

        assertNotNull(findCandidate)
        assertEquals(expectedFind, findCandidate)
    }

    @Test
    fun testDrop() {

        val dropCandidate = SqlMapper(Sql(), MYSQL).buildDrop(personMetadata)

        val expectedDrop = "DROP TABLE IF EXISTS $personUid"

        assertNotNull(dropCandidate)
        assertEquals(expectedDrop, dropCandidate)
    }

    companion object {
        private const val MYSQL = "mysql"
        @Suppress("unused")
        private const val ORACLE = "oracle"
    }
}
