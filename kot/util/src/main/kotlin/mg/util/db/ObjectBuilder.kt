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
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

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

        var isColumnsPrinted = false
        results.toOpt()
                .ifPresent(ResultSet::beforeFirst)
                .map(ResultSet::toResultSetIterator)
                .x { map { isColumnsPrinted = printlnColumnsAndRows(isColumnsPrinted, it) } }

        /*
            OBMultipleComposition(
                primitive = 555,
                oneToOne = OBSimple("1111"),
                oneToMany = listOf(OBSimpleComp("AAAA"), OBSimpleComp("BBBB"))
            )

            OBMultipleComposition.id
            OBMultipleComposition.primitive
            OBSimple.id
            OBSimple.simple
            OBSimpleComp.id
            OBSimpleComp.comp

            id  primitive   id  simple  id  comp id duplex
            1   555         1   1111    1   AAAA 1  A
            1   555         1   1111    1   BBBB 1  A
            1   555         1   1111    1   BBBB 1  B
            - uniqueByParent
                OBMultipleComposition -> OBSimple, OBSimpleComp
                OBSimpleComp -> OBDuplex

            OBMultipleComposition(
                primitive = 0,
                oneToOne = OBSimple(),
                oneToMany = listOf()
            )
            if (id == 1), process rows
            1#
            1   555         1   1111    1   AAAA 1  A
            OBMultipleComposition(
                primitive = 555,
                oneToOne = OBSimple(1111),
                oneToMany = listOf(OBSimpleComp(comp = AAAA, duplex = OBDuplex(A))
            )
            2#
            1   555         1   1111    1   BBBB 1  A
            OBMultipleComposition(
                primitive = 555,
                oneToOne = OBSimple(1111),
                oneToMany = listOf(OBSimpleComp(AAAA, duplex = OBDuplex(A)), OBSimpleComp(BBBB, duplex = listOf(OBDuplex(A))))
            )
            3#
            1   555         1   1111    1   BBBB 1  B
            OBMultipleComposition(
                primitive = 555,
                oneToOne = OBSimple(1111),
                oneToMany = listOf(OBSimpleComp(AAAA, duplex = OBDuplex(A)), OBSimpleComp(BBBB, duplex = listOf(OBDuplex(A), OBDuplex(B))))
            )

         */

        println()
        println("111:: $typeT")

        val primitivesOfTypeT = typeT::class.memberProperties
                .toCollection(ArrayList())
                .mapNotNull { it.javaField }
                .filter(!Common::isCustom)

        results.toOpt()
                .ifPresent(ResultSet::beforeFirst)
                .map(ResultSet::toResultSetIterator)
                .x {
                    map {
                        primitivesOfTypeT.forEach { field ->
                            getByFieldName(field, it).toOpt()
                                    .map { FieldAccessor.fieldSet(field, typeT, it) }
                        }
                    }
                }

        println("222:: $typeT")

        val uniquesByParent = HashMap<Any, MutableList<Any>>()

        collectUniquesByParent(typeT, uniquesByParent)

        uniquesByParent.entries.forEach { entry: MutableMap.MutableEntry<Any, MutableList<Any>> ->
            println("parent: ${entry.key::class.simpleName}")
            println("children: ${entry.value.joinToString { "" + it::class.simpleName }}")

            val allMembers = entry.key::class.memberProperties

            val listMembers = allMembers
                    .filter {
                        it.returnType.jvmErasure.isSubclassOf(List::class)
                    }.filter { kProperty1 ->
                        kProperty1.toOpt()
                                .map { it.javaField }
                                .map { FieldAccessor.fieldGet(it, entry.key) }
                                .mapTo(List::class)
                                .filter(List<*>::isNotEmpty)
                                .map(List<*>::first)
                                .filter(Common::hasCustomPackageName)
                                .isPresent()
                    }

            println()
            println("listMembers: $listMembers")

            val customs = allMembers.minus(listMembers)
                    .filter { kProperty1 ->
                        kProperty1.toOpt()
                                .map { it.javaField }
                                .map { FieldAccessor.fieldGet(it, entry.key) }
                                .filter(Common::hasCustomPackageName)
                                .isPresent()
                    }

            println()
            println("customs: $customs")
        }

        return mutableListOf()
    }

    /**
     * Doubles the given element i.
     */
    private fun doubleAnElement(i: Int): Int = i * 2

    private fun printlnColumnsAndRows(isColumnsPrinted: Boolean, rs: ResultSet): Boolean {
        var isPrinted = isColumnsPrinted
        if (!isPrinted) {
            (1..rs.metaData.columnCount).forEach { print("${rs.metaData.getColumnName(it)} ") }
            println()
            isPrinted = true
        }
        (1..rs.metaData.columnCount).forEach { print(rs.getString(it) + " ") }
        println()
        return isPrinted
    }

    private fun getByFieldName(field: Field, results: ResultSet): Any? =
            when (field.type.simpleName.lowercase()) {
                STRING -> results.getString(field.name)
                INT -> results.getInt(field.name)
                // LONG?
                // DATETIME?
                else -> null
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
                    .filter(Common::hasCustomPackageName)
                    .ifPresent { addElementToListIfNotExists(uniquesByParent, typeT, it) }
        }
    }

    private fun <T : Any> addElementToListIfNotExists(uniquesByParent: HashMap<Any, MutableList<Any>>, typeT: T, obj: Any) {
        uniquesByParent.toOpt()
                .filter { it.containsKey(typeT) }
                .map { it[typeT] }
                .filter { isNoSameUniqueInList(it, obj) }
                .ifPresent { it.add(obj) }
                .ifMissing { uniquesByParent[typeT] = mutableListOf(obj) }
    }

    private fun isNoSameUniqueInList(uniques: MutableList<Any>, obj: Any) =
            uniques.none { it::class.simpleName.equals(obj::class.simpleName, ignoreCase = true) }

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


