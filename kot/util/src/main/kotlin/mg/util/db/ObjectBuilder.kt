package mg.util.db

import mg.util.common.Common
import mg.util.common.Common.isMultiDepthCustom
import mg.util.common.Wrap
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
        //          DBOPerson3("A", "AA"),
        //          DBOPerson3("B", "BB"),
        //          DBOPerson3("C", "CC")
        //      )
        // )
        // id amount id firstName lastName
        // 1  10     1  A         AA
        // 1  10     2  B         BB
        // 1  10     3  C         CC
        // 2  20     4  D         DD
        // 2  20     5  E         EE
        // 2  20     6  C         FF
        // Billing(amount, persons[Person, Person])

//
//        val newInstance = typeT::class.createInstance()
//
//        val propertiesOfTypeT = typeT::class.declaredMemberProperties
//                .toCollection(ArrayList())
//                .filter(::isNotListCollectionOrArray)
//
//        println("YYY1:: $newInstance")
//
//        results?.beforeFirst()
//        if (results?.next() == true) {
//            propertiesOfTypeT.forEach {
//                val propertyValue = results.getString(it.name)
//                it.javaField?.isAccessible = true
//                it.javaField?.set(newInstance, propertyValue)
//            }
//        }
//        println("YYY2:: $newInstance")

        val uniquesByParent = HashMap<Any, MutableList<Any>>()

        collectUniquesByParent(typeT, uniquesByParent)

        uniquesByParent.entries.forEach {
            println("key: ${it.key::class.simpleName}")
            println("value: ${it.value.first()::class.simpleName}")
        }

        return mutableListOf()
    }

    private fun <T : Any> collectUniquesByParent(typeT: T, uniquesByParent: HashMap<Any, MutableList<Any>>) {

        val fieldsOfT = typeT::class.memberProperties
                .toMutableList()
                .mapNotNull { it.javaField }

        println("typeT: $typeT")

        fieldsOfT.filter(Common::isCustom)
                .map { field: Field -> getFieldValue(field, typeT) }
                .forEach { addElementToListIfNotExists(uniquesByParent, typeT, it) }

        // list types accepted for now only
        fieldsOfT.map { field ->
            getFieldValue(field, typeT).toOpt()
                    .mapTo(List::class)
                    .filter(List<*>::isNotEmpty)
                    .map(List<*>::first)
                    .filter { obj ->
                        listOf("kotlin.", "java.").none { it == obj::class.java.packageName }
                    }
                    .ifPresent { firstListElement ->
                        addElementToListIfNotExists(uniquesByParent, typeT, firstListElement)
                    }
        }

    }

    private fun <T : Any> getFieldValue(field: Field, typeT: T): Any {
        field.isAccessible = true
        return field.get(typeT)
    }

    private fun <T : Any> addElementToListIfNotExists(uniquesByParent: HashMap<Any, MutableList<Any>>, typeT: T, obj: Any) {
        if (uniquesByParent.containsKey(typeT)) {
            // if (uniquesByParent[typeT]?.contains(obj) == false) {
            uniquesByParent[typeT]?.add(obj)
            // }
        } else {
            uniquesByParent[typeT] = mutableListOf(obj)
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
    }
}


