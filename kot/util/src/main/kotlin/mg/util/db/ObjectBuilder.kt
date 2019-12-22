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

    private data class ConstructorData(val type: Any, val name: String)

    fun <T : Any> buildListOfT(results: ResultSet?, typeT: T): MutableList<T> {

        val constructorForT = narrowDownConstructorForT(results, typeT)

        val listT = mutableListOf<T>()
        do {
            val parametersListForT = getParameters(results)

            listT += createT(constructorForT, parametersListForT, typeT)

        } while (true == results?.next())

        return listT
    }

    private fun <T : Any> narrowDownConstructorForT(results: ResultSet?, t: T): Wrap<Constructor<T>?> {
        val constructor = Opt2.of(results)
                .map(ResultSet::getMetaData)
                .map(::getConstructorData)
                .mapWith(t) { data, type -> narrowDown(type::class.constructors, data) }
                .filter { it.t != null }
                .getOrElseThrow { Exception("Unable to narrow down a constructor for object T") }!!
        return constructor
    }

    private fun <T : Any> createT(constructor: Wrap<Constructor<T>?>, parametersList: MutableList<Any>, typeT: T) : T {
        return Opt2.of<Constructor<*>>(constructor.t)
                .mapWith(parametersList.toTypedArray()) { cons, params -> cons.newInstance(*params) } // spread operator
                .ifMissingThrow { Exception("Unable to instantiate ${typeT::class}") }
                .get() as T
    }

    private fun getParameters(results: ResultSet?): MutableList<Any> {
        val parametersList = mutableListOf<Any>()
        (1..getColumnCount(results))
                .filter { isColumnNameNotId(results, it) }
                .forEach {
                    parametersList += results?.getString(it) as Any // TOIMPROVE: constructor parameter names from the type?
                }
        return parametersList
    }

    private fun isColumnNameNotId(results: ResultSet?, it: Int) = results?.metaData?.getColumnName(it) != "id"
    private fun getColumnCount(results: ResultSet?) = results?.metaData?.columnCount ?: 1

    private fun <T : Any> narrowDown(constr: Collection<KFunction<T>>,
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
                list += ConstructorData(resultSetMetadata.getColumnClassName(i), resultSetMetadata.getColumnName(i))
            }
        }
        return list
    }

    private companion object {
        const val DB_ID_FIELD = "id"
    }
}
