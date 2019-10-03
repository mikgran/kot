package mg.util.db

import mg.util.functional.Opt2
import java.sql.Connection
import java.sql.ResultSet
import kotlin.text.toInt

// TOCONSIDER: remove wrapper?
// Very crude type T persistence solution
class DB() {

    private var dbConfig: DBConfig = DBConfig(Config())

    fun <T> save(any: T): Int {



        return 0 // default signal for zero changed objects and-or rows
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> find(any: T): T {

        require(!connection.isClosed) { "Find: Connection was closed." }


        return Any() as T
    }

}