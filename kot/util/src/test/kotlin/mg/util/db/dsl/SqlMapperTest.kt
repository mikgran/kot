package mg.util.db.dsl

import mg.util.db.AliasBuilder
import mg.util.db.DBO
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class SqlMapperTest {

    private val person = SMTPerson("testname1", "testname2")
    private val dbo = DBO(DefaultDslMapper("mysql"))
    private val personUid = UidBuilder.build(SMTPerson::class)
    private val personAlias = AliasBuilder.build(personUid)
    private val personMetadata = dbo.buildMetadata(person)

    @Test
    fun testCreateTable() {

        val createTableSqlCandidate = DefaultDslMapper(MYSQL).buildCreateTable(personMetadata)

        assertNotNull(createTableSqlCandidate)
        assertEquals("CREATE TABLE IF NOT EXISTS ${personMetadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(64) NOT NULL, lastName VARCHAR(64) NOT NULL)", createTableSqlCandidate)

        // TOIMPROVE: test coverage for exceptions
    }

    @Test
    fun testBuildInsert() {

        val insertCandidate = DefaultDslMapper(MYSQL).buildInsert(personMetadata)

        val expectedInsert = "INSERT INTO ${personMetadata.uid} (firstName, lastName) VALUES ('testname1', 'testname2')"

        assertNotNull(insertCandidate)
        assertEquals(expectedInsert, insertCandidate)
    }

    @Test
    fun testFinding() {

        val findCandidate = DefaultDslMapper(MYSQL).buildFind(personMetadata)

        val expectedFind = "SELECT $personAlias.firstName, $personAlias.lastName FROM $personUid $personAlias"

        assertNotNull(findCandidate)
        assertEquals(expectedFind, findCandidate)
    }

    @Test
    fun testDrop() {

        val dropCandidate = DefaultDslMapper(MYSQL).buildDrop(personMetadata)

        val expectedDrop = "DROP TABLE IF EXISTS $personUid"

        assertNotNull(dropCandidate)
        assertEquals(expectedDrop, dropCandidate)
    }

    @Test
    fun testBuildShowColumns() {

        val columnsCandidate = DefaultDslMapper(MYSQL).buildShowColumns(personMetadata)

        val expectedShowColumns = "SHOW COLUMNS FROM $personUid"

        assertNotNull(columnsCandidate)
        assertEquals(expectedShowColumns, columnsCandidate)
    }

    companion object {
        private const val MYSQL = "mysql"

        @Suppress("unused")
        private const val ORACLE = "oracle"
    }
}
