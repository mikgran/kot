package mg.util.db

import mg.util.db.DBTest.PersonB
import mg.util.functional.Opt2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ObjectBuilderTest {

    private val dbo = DBO(SqlMapperFactory.get("mysql"))
    private val dbConfig = DBConfig(TestConfig())

    @Test
    fun testBuildT() {

        val connection = dbConfig.connection
        dbo.ensureTable(PersonB("", ""), connection)
        dbo.save(PersonB(firstName, lastName), connection)
        val uid = dbo.buildUniqueId(PersonB())

        val results = Opt2.of(connection.createStatement())
                .map { it.executeQuery("SELECT * FROM $uid") }
                .filter { it.next() }
                .getOrElseThrow { Exception("no results in table $uid.") }

        val listT = ObjectBuilder().buildListOfT(results, PersonB())

        assertTrue(listT.isNotEmpty())
        assertTrue(containsFirstNameLastName(listT))
    }

    private fun containsFirstNameLastName(candidateMapped: List<PersonB>): Boolean {
        var found = false
        candidateMapped.forEach {
            if (firstName == it.firstName && lastName == it.lastName) {
                found = true
            }
        }
        return found
    }

    companion object {
        private const val firstName = "cc--"
        private const val lastName = "dd--"
    }


}