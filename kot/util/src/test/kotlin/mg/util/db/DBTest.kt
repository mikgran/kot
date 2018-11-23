package mg.util.db

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DBTest {

    private var dbConfig: DBConfig = DBConfig(TestConfig())

    abstract class DBO(open val id: Long = 0, val fetched: Boolean = false)

    data class Person(val firstName: String = "", val lastName: String = "") : DBO()

    @Test
    fun test_saving() {

        Assertions.assertDoesNotThrow {

            val connection = dbConfig.connection
            val db = DB(connection)

            db.find(Person("name1", "name2"))

            //db.save(Person("firstName", "lastName"))

        }


    }

}