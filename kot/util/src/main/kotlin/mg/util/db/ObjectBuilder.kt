package mg.util.db

import mg.util.common.Wrap
import mg.util.functional.Opt2
import java.lang.reflect.Constructor
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType

@Suppress("UNCHECKED_CAST")
class ObjectBuilder {

    data class ConstructorData(val type: Any, val name: String)

    fun <T : Any> buildListOfT(results: ResultSet?, t: T): MutableList<T> {

        val constructor = Opt2.of(results)
                .map(ResultSet::getMetaData)
                .map(::getConstructorData)
                .mapWith(t) { data, type -> getConstructor(type::class.constructors, data) }
                .filter { it.t != null }
                .getOrElseThrow { Exception("Unable to narrow down a constructor for object T") }!!

        val listT = mutableListOf<T>()
        do {
            val parameters = getParameters(results)

            buildAndAddTypeToList(constructor.t, parameters, listT, t)

        } while (true == results?.next())

        return listT
    }

    private fun <T : Any> getConstructor(constr: Collection<KFunction<T>>,
                                         rscd: MutableList<ConstructorData>): Wrap<Constructor<T>?> {
        val result = Wrap<Constructor<T>?>(null)
        constr.map { c ->
            val constructorDatas = getConstructorDatas(c)
            if (constructorDatas.containsAll(rscd)) { // TOIMPROVE: test coverage
                result.t = c.javaConstructor
            }
        }
        return result
    }

    private fun <T : Any> getConstructorDatas(c: KFunction<T>): MutableList<ConstructorData> {
        val constructorDataCr = mutableListOf<ConstructorData>()
        c.parameters.forEach { p ->
            constructorDataCr.add(ConstructorData(p.type.javaType.typeName, p.name ?: ""))
        }
        return constructorDataCr
    }

    private fun getConstructorData(resultSetMetadata: ResultSetMetaData): MutableList<ConstructorData> {
        val list = mutableListOf<ConstructorData>()
        (1..resultSetMetadata.columnCount).forEach { i ->
            if (resultSetMetadata.getColumnName(i) != DB_ID_FIELD) {
                list.add(ConstructorData(resultSetMetadata.getColumnClassName(i), resultSetMetadata.getColumnName(i)))
            }
        }
        return list
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
                parameters += results?.getString(i) as Any // TOIMPROVE: constructor parameter names from the type?
            }
        }
        return parameters.toTypedArray()
    }

    companion object {
        const val DB_ID_FIELD = "id"
    }
}
