package mg.util.db

import mg.util.common.Common
import mg.util.common.Common.isMultiDepthCustom
import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.Wrap
import mg.util.db.dsl.FieldAccessor
import mg.util.db.functional.toResultSetIterator
import mg.util.functional.toOpt
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

@Suppress("UNCHECKED_CAST")
open class ObjectBuilder {

    protected data class ConstructorData(val type: Any, val name: String)

    fun <T : Any> buildListOfT(results: ResultSet?, typeT: T): MutableList<T> {
        return typeT
                .toOpt()
                .case({ it is String }, { println("\nA\n"); buildListUsingStrings(results, it) })
                .case({ isMultiDepthCustom(it) }, { println("\nB\n"); buildListOfMultiDepthCustoms(results, it) })
                .caseDefault { println("\nC\n"); buildListOfSingleDepthCustoms(results, it) }
                .result()
                .getOrElse(mutableListOf())
    }

    private fun <T : Any> buildListOfMultiDepthCustoms(results: ResultSet?, typeT: T): MutableList<T> {

        // DBOBilling2(
        //      "10",
        //      listOf(
        //          DBOPerson3("A", "AA", listOf(
        //                                       DBODelivery("e"),
        //                                       DBODelivery("p"),
        //                                       DBODelivery("u")
        //                                ),
        //          DBOPerson3("B", "BB", listOf(
        //                                       DBODelivery("p")
        //                                ),
        //          DBOPerson3("C", "CC", listOf(
        //                                       DBODelivery("p")
        //                                )
        //      )
        // )
        // id amount id firstName lastName delivery
        // 1  10     1  A         AA       e
        // 1  10     1  A         AA       p
        // 1  10     1  A         AA       u
        // 1  10     2  B         BB       p
        // 1  10     3  C         CC       p
        // 2  10     1  A         AA       p
        // 2  10     2  B         BB       p
        // 2  10     3  C         CC       p

        // Billing(amount, persons[Person, Person, Person])

        println("111:: $typeT")

        val propertiesOfTypeT = typeT::class.memberProperties
                .toCollection(ArrayList())
                .mapNotNull { it.javaField }
                .filter(!Common::isCustom)

        results.toOpt()
                .ifPresent (ResultSet::beforeFirst)
                .filter(ResultSet::next)
                .mapWith(propertiesOfTypeT) { results, fields ->
                    fields.forEach { field ->
                        if (field.type.simpleName.lowercase() == INT) {
                            FieldAccessor.fieldSet(field, typeT, results.getInt(field.name))
                        }
                        if (field.type.simpleName.lowercase() == STRING) {
                            FieldAccessor.fieldSet(field, typeT, results.getString(field.name))
                        }
                    }
                }

        println("222:: $typeT")

        val uniquesByParent = HashMap<Any, MutableList<Any>>()

        collectUniquesByParent(typeT, uniquesByParent)

        uniquesByParent.entries.forEach { entry ->
            println("key: ${entry.key::class.simpleName}")
            println("value: ${entry.value.joinToString { " " + it::class.simpleName }}")
        }



        return mutableListOf()
    }

    private fun <T : Any> collectUniquesByParent(typeT: T, uniquesByParent: HashMap<Any, MutableList<Any>>) {

        val fieldsOfT = typeT::class.memberProperties
                .toMutableList()
                .mapNotNull { it.javaField }

        fieldsOfT.filter(Common::isCustom)
                .map { field: Field -> FieldAccessor.fieldGet(field, typeT) }
                .forEach { addElementToListIfNotExists(uniquesByParent, typeT, it) }

        // list types accepted for now only
        fieldsOfT.map { field ->

            FieldAccessor.fieldGet(field, typeT).toOpt()
                    .mapTo(List::class)
                    .filter(List<*>::isNotEmpty)
                    .map(List<*>::first)
                    .filter(::hasCustomPackageName)
                    .ifPresent { addElementToListIfNotExists(uniquesByParent, typeT, it) }
        }
    }

    private fun hasCustomPackageName(obj: Any) =
            listOf("kotlin.", "java.").none { obj::class.java.packageName.contains(it, ignoreCase = true) }

    private fun <T : Any> addElementToListIfNotExists(uniquesByParent: HashMap<Any, MutableList<Any>>, typeT: T, obj: Any) {
        when {
            uniquesByParent.containsKey(typeT) -> uniquesByParent[typeT]?.add(obj)
            else -> uniquesByParent[typeT] = mutableListOf(obj)
        }
    }

    private fun <T : Any> buildListUsingStrings(results: ResultSet?, typeT: T): MutableList<T> {

        val list = mutableListOf<T>()

        results.toOpt()
                .map(ResultSet::toResultSetIterator)
                .xmap { forEach { addToList(it, typeT, list) } }

        return list
    }

    private fun <T : Any> addToList(resultSet: ResultSet, typeT: T, list: MutableList<T>) {
        resultSet.getString(1)
                .toOpt()
                .mapTo(typeT::class)
                .map(list::add)
    }

    private fun <T : Any> buildListOfSingleDepthCustoms(results: ResultSet?, typeT: T): MutableList<T> {

        val listT = mutableListOf<T>()
        val constructorForT = narrowDownConstructorForT(results, typeT)
        val columnCount = results?.metaData?.columnCount ?: 1

        do {
            val parametersListForT = getParameters(results, columnCount)
            listT += createT(constructorForT, parametersListForT, typeT)

        } while (true == results?.next())

        return listT
    }

    private fun <T : Any> narrowDownConstructorForT(results: ResultSet?, typeT: T): Wrap<Constructor<T>?> {
        return results
                .toOpt()
                .map(ResultSet::getMetaData)
                .map(::getFields)
                .mapWith(typeT) { fields, type -> narrowDown(fields, type::class.constructors) }
                .filter { it.t != null }
                .getOrElseThrow { Exception("Unable to narrow down a constructor for object T: ${typeT::class}") }!!
    }

    private fun <T : Any> createT(constructor: Wrap<Constructor<T>?>, parametersList: MutableList<Any>, typeT: T): T {
        return constructor.t
                .toOpt()
                .mapWith(parametersList.toTypedArray()) { cons, params -> cons.newInstance(*params) } // spread operator
                .ifMissingThrow { Exception("Unable to instantiate ${typeT::class}") }
                .get() as T
    }

    private fun getParameters(results: ResultSet?, columnCount: Int): MutableList<Any> {
        val parametersList = mutableListOf<Any>()
        (1..columnCount)
                .filter { isColumnNameNotId(results, it) }
                .forEach {
                    parametersList += results?.getString(it) as Any // TOIMPROVE: constructor parameter names from the type?
                }
        return parametersList
    }

    private fun isColumnNameNotId(results: ResultSet?, columnNumber: Int) = results?.metaData?.getColumnName(columnNumber) != "id"

    private fun <T : Any> narrowDown(
            resultSetConstructorData: MutableList<ConstructorData>,
            constr: Collection<KFunction<T>>,
    ): Wrap<Constructor<T>?> {
        val result = Wrap<Constructor<T>?>(null)
        constr.map { c ->
            val constructorDatas = getConstructorDatas(c)
            if (constructorDatas.containsAll(resultSetConstructorData)) { // TOIMPROVE: test coverage
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

    private fun getFields(resultSetMetadata: ResultSetMetaData): MutableList<ConstructorData> {
        val list = mutableListOf<ConstructorData>()
        (1..resultSetMetadata.columnCount).forEach { i ->
            if (resultSetMetadata.getColumnName(i) != DB_ID_FIELD) { // add coverage for various types of db providers
                list += ConstructorData(resultSetMetadata.getColumnClassName(i), resultSetMetadata.getColumnName(i))
            }
        }
        return list
    }

    private companion object {
        const val DB_ID_FIELD = "id"
        const val INT = "int"
        const val STRING = "string"
    }
}


