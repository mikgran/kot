package mg.util.db

import mg.util.functional.Opt2
import org.junit.Ignore
// import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.ResultSet

internal class DBTest {

    private var dbConfig: DBConfig = DBConfig(TestConfig())

    data class Person(val firstName: String = "", val lastName: String = "") : DBO()

    // @Test
    fun test_saving() {

        assertDoesNotThrow {

            val connection = dbConfig.connection

            print(":: ${dbConfig.properties}")

            val db = DB(connection)

            // db.find(Person("name1", "name2"))

            val fName = "firstName"
            val lName = "lastName"
            val queryString = "SELECT persons.firstname, persons.lastname FROM persons"

            // creates tables and sets buildMetadata
            db.save(Person(fName, lName))

            val candidate = Opt2.of(connection)
                    .map { connection.createStatement() }
                    .map { statement -> statement.executeQuery(queryString) }
                    .filter(ResultSet::next)
                    .map { resultSet -> resultSet.getString(1) + " " + resultSet.getString(2) }
                    .getOrElse("")

            assertEquals("$fName $lName", candidate)

            // assert person table exists
            // assert person table has firstName and lastName columns

            // test coverage: assert object relations and their table structure
            //  exists.
        }


    }

}