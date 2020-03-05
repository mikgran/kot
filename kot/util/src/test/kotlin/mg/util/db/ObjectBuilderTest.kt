package mg.util.db

import mg.util.db.TestDataClasses.OBPersonB
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.SqlMapper
import mg.util.functional.Opt2
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ObjectBuilderTest {

    private val dbo = DBO(SqlMapper("mysql"))
    private val dbConfig = DBConfig(TestConfig())

    @Test
    fun testBuildT() {

        val connection = dbConfig.connection
        dbo.ensureTable(OBPersonB("", ""), connection)
        dbo.save(OBPersonB(firstName, lastName), connection)
        val uid = buildUniqueId(OBPersonB())

        val results = Opt2.of(connection.createStatement())
                .map { it.executeQuery("SELECT * FROM $uid") }
                .filter { it.next() }
                .getOrElseThrow { Exception("no results in table $uid.") }

        val listT = ObjectBuilder().buildListOfT(results, OBPersonB())

        assertTrue(listT.isNotEmpty())
        assertTrue(containsFirstNameLastName(listT))
    }

    private fun containsFirstNameLastName(candidateMapped: List<OBPersonB>): Boolean {
        var found = false
        candidateMapped.forEach {
            if (firstName == it.firstName && lastName == it.lastName) {
                found = true
            }
        }
        return found
    }

    @Suppress("unused")
    companion object {

        private const val firstName = "cc--"
        private const val lastName = "dd--"

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            TestSupport.dropTables(listOf(OBPersonB()))
        }
    }

}