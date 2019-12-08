package mg.util.db.functional

import mg.util.db.DBConfig
import mg.util.db.DBO
import mg.util.db.DBOTest.Person
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.SqlMapperFactory
import mg.util.db.functional.ResultSetIterator.Companion.iof
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class ResultSetIteratorTest {

    private val dbConfig = DBConfig.testConfig
    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testIteratingResultSet() {

        val person1 = Person("test1", "test11")
        val person2 = Person("test2", "test33")
        val connection = dbConfig.connection

        with(dbo) {
            ensureTable(person1, connection)
            save(person1, connection)
            save(person2, connection)
        }

        val uid = buildUniqueId(Person())
        of(connection)
                .map(Connection::createStatement)
                .mapWith(uid) { s, u -> s.executeQuery("SELECT * FROM $u") }
                .map(::iof)
                .xm { map { Person(it.getString(2), it.getString(3)) } }
                .apply {
                    assertTrue(get()!!.isNotEmpty())
                    assertTrue(get()!!.containsAll(listOf(person1, person2)))
                }
    }

}