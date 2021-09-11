package mg.util.db

import mg.util.common.Common
import mg.util.db.TestDataClasses.*
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.db.dsl.DefaultDslMapper
import mg.util.db.dsl.DslMapperFactory
import mg.util.db.dsl.Sql
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
        val obPersonB = cleaner.register(OBPersonB("", ""))

        dbo.ensureTable(obPersonB, connection)
        dbo.save(OBPersonB(firstName, lastName), connection)

        val results = queryResults(OBPersonB(), connection)

        val listT = ObjectBuilder().buildListOfT(results, OBPersonB())

        assertTrue(listT.isNotEmpty())
        assertTrue(containsFirstNameLastName(listT))
    }

    @Test // FIXME 10000
    fun testBuildMultiDepthCustom() {

        val obMultipleComposition =
                OBMultipleComposition(
                        compositionValue = 555,
                        obSimple = OBSimple(simple = "1111"),
                        obSimpleComps = listOf(
                                OBSimpleComp(comp = "AAAA", sub = OBSubComp(sub = 77)),
                                OBSimpleComp(comp = "BBBB", sub = OBSubComp(sub = 88)))
                )

        val connection = dbConfig.connection
        val obMultipleComp = OBMultipleComposition()
        cleaner.registerRelational(OBMultipleComposition())

        dbo.ensureTable(obMultipleComp, connection)
        dbo.save(obMultipleComposition, connection)

        val results = queryResults(obMultipleComposition, connection)

        val listT = ObjectBuilder().buildListOfT(results, obMultipleComp)

        assertTrue(listT.isNotEmpty(), "while calling buildListOfT OBMultipleComposition from results building should produce at least one object")
        // assertTrue(containsFirstNameLastName(listT))
    }

    private fun queryResults(t: Any, connection: Connection): ResultSet {
        val uid = buildUniqueId(t)
        val sql = Sql select t
        sql.parameters().isPrimaryIdIncluded = true
        val sqlStr = DslMapperFactory.get().map(sql)

        val delimiters = listOf("JOIN", "AND", "FROM", "ON")
        Common.splitWithDelimiters(sqlStr, delimiters)
                .joinToString("\n")

        return Opt2.of(connection.createStatement())
                .map { it.executeQuery(sqlStr) }
                .filter { it.next() }
                .ifMissingThrow { Exception("no results in table $uid.") }
                .value()
    }

    private fun containsFirstNameLastName(candidateMapped: List<OBPersonB>): Boolean {
        return candidateMapped.any {
            firstName == it.firstName && lastName == it.lastName
        }
    }

    @Suppress("unused")
    companion object {
        val cleaner = TableDrop()

        private const val firstName = "cc--"
        private const val lastName = "dd--"

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            cleaner.dropAll()
        }
    }

}