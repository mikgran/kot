package mg.util.db

import mg.util.functional.Opt
import mg.util.functional.Opt2
import java.lang.IllegalArgumentException
import java.sql.Connection

// Very crude object persistence solution
class DB(connection: Connection) {

    val connection = connection

    fun <T> save(any: T): Int {

        return 0 // default signal for zero changed objects/rows
    }

    inline fun <reified T : Any> find(any: T): T {

        if (connection.isClosed) {
            throw IllegalArgumentException("Find: Connection was closed.")
        }

        val select1 = Opt.of(connection.createStatement())
                .map { it?.executeQuery("select 1") }
                .filter { it?.next() ?: false }
                .map { it?.getString(1) }
                .getOrElse("no results")

        val select2 = Opt2.of(connection.createStatement())
                .map { it.executeQuery("select 2") }
                .filter { it.next() }
                .map { it.getString(1) }
                .getOrElse("no results2")


        println(select1)
        println(select2)

//        val statement = connection.createStatement()
//        val result = statement.executeQuery("select 1")
//        if (result.next()) {
//            val string = result.getString(1)
//            println(string)
//        }

        return T::class.java.getConstructor().newInstance()

        // ugly as fuck, but hey, reified½!
        // return T::class.java.getDeclaredConstructor().newInstance()
    }

}