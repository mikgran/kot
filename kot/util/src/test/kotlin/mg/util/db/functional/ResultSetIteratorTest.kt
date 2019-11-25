package mg.util.db.functional

import mg.util.db.DBConfig
import mg.util.db.DBO
import mg.util.db.DBOTest.Person
import mg.util.db.SqlMapperFactory
import mg.util.db.TestConfig
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection
import mg.util.db.functional.ResultSetIterator.Companion.of as iof

internal class ResultSetIteratorTest {

    private val dbConfig = DBConfig(TestConfig())
    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testIteratingResultSet() {

        val person1 = Person("test1", "test11")
        val person2 = Person("test2", "test33")
        val connection = dbConfig.connection

        dbo.ensureTable(person1, connection)
        dbo.save(person1, connection)
        dbo.save(person2, connection)

        val uid = buildUniqueId(Person())
        val candidates = of(connection)
                .map(Connection::createStatement)
                .mapWith(uid) { s, u -> s.executeQuery("SELECT * FROM $u") }
                .getAndMap(::iof)!!
                .map { Person(it.getString(2), it.getString(3)) }

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.containsAll(listOf(person1, person2)))
    }

}