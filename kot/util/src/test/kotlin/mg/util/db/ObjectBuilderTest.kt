package mg.util.db

import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.Opt2
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet

internal class ObjectBuilderTest {

    private val dbo = DBO(DefaultDslMapper("mysql"))
    private val dbConfig = DBConfig(TestConfig())

    @Test
    fun testBuildSingleDepthCustom() {

        val connection = dbConfig.connection
        dbo.ensureTable(OBPersonB("", ""), connection)
        dbo.save(OBPersonB(firstName, lastName), connection)

        val results = queryResults(OBPersonB(), connection)

        val listT = ObjectBuilder().buildListOfT(results, OBPersonB())

        assertTrue(listT.isNotEmpty())
        assertTrue(containsFirstNameLastName(listT))
    }

    @Test
    fun testBuildMultiDepthCustom() {

        val obMultipleComposition = OBMultipleComposition(555, OBSimple("1111"), listOf(OBSimpleComp("AAAA"), OBSimpleComp("BBBB")))

        val connection = dbConfig.connection
        dbo.ensureTable(OBMultipleComposition(), connection)
        dbo.save(obMultipleComposition, connection)

        val results = queryResults(obMultipleComposition, connection)

        val listT = ObjectBuilder().buildListOfT(results, OBMultipleComposition())

        assertTrue(listT.isNotEmpty(), "while building OBMultipleComposition from results should produce at least one object")
        // assertTrue(containsFirstNameLastName(listT))
    }

    private fun queryResults(t: Any, connection: Connection): ResultSet? {
        val uid = buildUniqueId(t)
        return Opt2.of(connection.createStatement())
                .map { it.executeQuery("SELECT * FROM $uid") }
                .filter { it.next() }
                .getOrElseThrow { Exception("no results in table $uid.") }
    }

    private fun containsFirstNameLastName(candidateMapped: List<OBPersonB>): Boolean {
        return candidateMapped.any {
            firstName == it.firstName && lastName == it.lastName
        }
    }

    @Suppress("unused")
    companion object {

        private const val firstName = "cc--"
        private const val lastName = "dd--"

        @AfterAll
        @JvmStatic
        internal fun afterAll() {

            val testTables = listOf(
                    OBPersonB(),
                    OBMultipleComposition(),
                    OBSimple(),
                    OBSimpleComp()
            )
            TestSupport.dropTables(testTables)

            val testJoinTables = listOf(
                    Pair(OBMultipleComposition(), OBSimple()),
                    Pair(OBMultipleComposition(), OBSimpleComp())
            )
            TestSupport.dropJoinTables(testJoinTables)
        }
    }

}