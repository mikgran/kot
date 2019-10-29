package mg.util.db

import mg.util.functional.Opt2
import java.lang.reflect.Constructor
import java.sql.ResultSet

@Suppress("UNCHECKED_CAST")
class ObjectBuilder {

    fun <T : Any> buildListOfT(results: ResultSet?, constructor: Constructor<*>?, t: T): MutableList<T> {
        val listT = mutableListOf<T>()
        do {
            val parameters = getParameters(results)

            buildAndAddTypeToList(constructor, parameters, listT, t)

        } while (true == results?.next())
        return listT
    }

    private fun <T : Any> buildAndAddTypeToList(constructor: Constructor<*>?, parameters: Array<Any>, listT: MutableList<T>, t: T) {
        Opt2.of(constructor)
                .map { it.newInstance(*parameters) } // spread operator
                .ifPresent { listT += it as T }
                .ifMissingThrow { Exception("Unable to instantiate ${t::class}") }
    }

    private fun getParameters(results: ResultSet?): Array<Any> {
        val parameters = mutableListOf<Any>()
        (1..(results?.metaData?.columnCount ?: 1)).forEach { i ->

            if (results?.metaData?.getColumnName(i) != "id") {
                parameters += results?.getString(i) as Any // TOIMPROVE: type
            }
        }
        return parameters.toTypedArray()
    }
}
