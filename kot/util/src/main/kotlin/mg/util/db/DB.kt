package mg.util.db

import mg.util.functional.Opt2
import java.sql.Connection
import java.sql.ResultSet
import kotlin.text.toInt

// Very crude object persistence solution
class DB(connection: Connection) {

    val connection = connection

    fun <T> save(any: T): Int {

        return 0 // default signal for zero changed objects/rows
    }

    fun <T : Any> find(any: T): T {

        if (connection.isClosed) {
            throw IllegalArgumentException("Find: Connection was closed.")
        }

//        val select1 = Opt.of(connection.createStatement())
//                .map { it?.executeQuery("select 1") }
//                .filter { it?.next() ?: false }
//                .map { it?.getString(1) }
//                .getOrElse("no results")

        val select2 = Opt2.of(connection.createStatement())
                .map { it.executeQuery("select 2") }
                .filter(ResultSet::next)
                .map { it.getString(1) }
                .match("", { it == "2" }, String::toInt)
                .result()
                .match(0, { it == 2 }, { it.toString() })
                .result()
                .getOrElse("default")


//        println(select1)
        println(select2)

//        val statement = connection.createStatement()
//        val result = statement.executeQuery("select 1")
//        if (result.next()) {
//            val string = result.getString(1)
//            println(string)
//        }

        return Any() as T

        // ugly as fuck, but hey, reified½!
        // return T::class.java.getDeclaredConstructor().newInstance()
    }

}