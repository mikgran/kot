package mg.util.db.dsl

import mg.util.common.Common.hasContent
import mg.util.common.Common.isCustom
import mg.util.common.Common.isList
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.FieldAccessor.Companion.fieldGet
import mg.util.db.dsl.FieldAccessor.Companion.getFieldsWithCustoms
import mg.util.db.dsl.FieldAccessor.Companion.getFieldsWithListOfCustoms
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.toOpt
import java.lang.reflect.Field
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class MySqlImpl {

    class Create(t: Any) : Sql.Create(t) {
        // TODO: 1 does not include multilayer creates yet
        override fun build(p: Parameters): String {
            return MySqlCreateBuilder().buildCreate(p, this)
        }
    }

    class ShowColumns(t: Any) : Sql.ShowColumns(t) {

        override fun build(p: Parameters): String {
            return "SHOW COLUMNS FROM ${UidBuilder.buildUniqueId(t)}"
        }
    }

    class Drop(t: Any) : Sql.Drop(t) {
        override fun build(p: Parameters): String {
            return "DROP TABLE IF EXISTS ${UidBuilder.buildUniqueId(t)}"
        }
    }

    class Insert(t: Any) : Sql.Insert(t) {
        override fun build(p: Parameters): String {

            val dp = DslParameters().apply {
                typeT = t
                uniqueId = UidBuilder.buildUniqueId(t)
                uniqueIdAlias = AliasBuilder.build(uniqueId!!)
            }

            // - collect map of lists of parent-child combinations
            // - none of the parent, parent-child contains any arrays, collections, sets
            // - insert parent
            // - insert any children with parent refs
            /*
            building - floor, floor, floor
            building - address
            address - street
             */

            val sqls = mutableListOf<String>()

            // FIXME 103
            // change to case a, b or c
            sqls += buildInsertSql(t)

            sqls += getFieldsWithCustoms(dp)
                    .map { field -> fieldGet(field, dp.typeT) }
                    .map { buildInsertSqlOneToOne(it, t) }

            // TODO fix the list processing and parentId usage
            sqls += getFieldsWithListOfCustoms(dp)
                    .map { field -> fieldGet(field, dp.typeT) as List<*> }
                    .flatten()
                    .filterNotNull()
                    .map { buildInsertSqlOneToOne(it, t) }

            return sqls.joinToString(";")
        }

        private fun buildInsertSql(type: Any): String =
                buildInsertSql(type) { typeUid, fields, fieldsValues ->
                    "INSERT INTO $typeUid ($fields) VALUES ($fieldsValues)"
                }

        private fun buildInsertSqlOneToOne(type: Any, parentType: Any): String =
                buildInsertSql(type) { typeUid, fields, fieldsValues ->

                    val parentUid = UidBuilder.buildUniqueId(parentType)
                    val tableJoinUid = parentUid + typeUid

                    "SELECT LAST_INSERT_ID() INTO @parentLastId;" +
                            "INSERT INTO $typeUid ($fields) VALUES ($fieldsValues);" +
                            "SELECT LAST_INSERT_ID() INTO @childLastId;" +
                            "INSERT INTO $tableJoinUid (${parentUid}refid, ${typeUid}refid) VALUES (@parentLastId, @childLastId)"
                }

        // TODO: 90 add test coverage: one-to-many relation
        private fun buildInsertSqlOneToMany(children: List<Any>, type: Any): String {
            return buildInsertSql(type) { typeUid, fields, fieldsValues ->

                "INSERT INTO $typeUid ($fields) VALUES ($fieldsValues)"
            }
        }

        private fun buildInsertSql(type: Any, insertCreateFunction: (String, Opt2<String>, Opt2<String>) -> String): String {

            val typeName = type::class.simpleName
            if (typeName?.contains("array", ignoreCase = true) == true) {
                return ""
            }

            val typeUid = UidBuilder.buildUniqueId(type)

            val properties = getNonArrayNonCollectionMemberProperties(type)

            val fields = properties.map { it.joinToString(", ") { p -> p.name } }

            val fieldsValues = getFieldsValuesAsStringCommaSeparated(properties, type)

            return insertCreateFunction(typeUid, fields, fieldsValues)
        }

        private fun getFieldsValuesAsStringCommaSeparated(properties: Opt2<List<KProperty1<*, *>>>, type: Any): Opt2<String> =
                properties.map { list: List<KProperty1<*, *>> ->
                    list.joinToString(", ") {
                        "'${getFieldValueAsString(it, type)}'"
                    }
                }

        private fun getNonArrayNonCollectionMemberProperties(type: Any): Opt2<List<KProperty1<*, *>>> {
            return type.toOpt()
                    .map { it::class.memberProperties }
                    .lfilter { p: KProperty1<*, *> ->
                        p.javaField != null
                                && !isCustom(p.javaField!!)
                                && !p::returnType.name.contains("Array")
                    }
        }
    }

    class Delete(t: Any) : Sql.Delete(t) {
        override fun build(p: Parameters): String {
            // no multi table deletes supported
            // DELETE FROM table where field = value AND field2 = value2
            val sb = StringBuilder() +
                    "DELETE FROM ${UidBuilder.buildUniqueId(t)} " +
                    p.whereFragments.joinToString(" ")
            return sb.toString()
        }

        class Where(t: Any) : Delete.Where(t) {
            override fun build(p: Parameters): String {
                p.whereFragments += of(t as? KProperty1<*, *>)
                        .map { "WHERE ${it.name}" }
                        .getOrElse("")
                return ""
            }

            class Eq(t: Any) : Delete.Where.Eq(t) {
                override fun build(p: Parameters): String {
                    p.whereFragments += "= '$t'"
                    return ""
                }
            }
        }
    }

    // FIXME: 104 SELECT natural vs manual
    class Select(t: Any) : Sql.Select(t) {
        override fun build(p: Parameters): String {

            p.tableFragments.add(0, buildTableFragment(t))
            p.joinsMap.putAll(buildJoinsMap(t, p))

            p.toOpt()
                    .map { buildJoinsForNaturalRefs(it) }
                    .filter(String::isNotEmpty)
                    .map(p.joinFragments::add)

            collectUniqueTypesFrom(p.action, p.joinsMap)
                    .forEach { p.columnFragments += buildFieldPart(it) }

            val sb = StringBuilder() +
                    "SELECT ${p.columnFragments.joinToString(", ")}" +
                    " FROM ${p.tableFragments.joinToString(", ")}" +
                    buildJoinPart(p) +
                    buildManualJoinPart(p) +
                    buildWherePart(p)
            return sb.toString()
        }

        private fun buildManualJoinPart(p: Parameters): String =
                if (p.manualJoinFragments.isNotEmpty()) " ${p.manualJoinFragments.joinToString(" ")}" else ""

        private fun buildTableFragment(type: Any): String {
            val (uid, alias) = buildUidAndAlias(type)
            return "$uid $alias"
        }

        private fun buildJoinsMap(root: Any, p: Parameters): MutableMap<Any, List<Any>> {

            val joinsMap = mutableMapOf<Any, List<Any>>()
            root.toOpt()
                    .map {
                        val list: List<Any> = childrenForParent(it)
                        if (list.isNotEmpty()) {
                            joinsMap[it] = list
                        }
                        list
                    }
                    .xmap { forEach { buildJoinsMap(it, p) } }
            return joinsMap
        }

        private fun childrenForParent(type: Any): List<Any> {
            return type::class.java.declaredFields.toList()
                    .toOpt()
                    .lfilter(::isCustom or ::isList)
                    .lxmap<Field, Any> { mapNotNull { getFieldValue(it, type) } }
                    .getOrElse { emptyList() }
        }

        private fun collectUniqueTypesFrom(action: Sql?, joinsMap: MutableMap<*, *>): MutableSet<Any> {
            val uniques = mutableSetOf<Any>()
            action?.t.toOpt()
                    .map(uniques::add)

            joinsMap.iterator()
                    .toOpt()
                    .lmap { entry: MutableMap.MutableEntry<Any, Any> -> getUniques(entry, uniques) }

            return uniques
        }

        private fun getUniques(entry: MutableMap.MutableEntry<Any, Any>, uniques: MutableSet<Any>) {
            when (val e = entry.value) {
                is List<*> -> getUniques(e, uniques)
                else -> uniques.add(e)
            }
        }

        private fun getUniques(list: List<*>, uniques: MutableSet<Any>) {
            list.forEach {
                when (it) {
                    is List<*> -> getUniques(it, uniques)
                    else -> it?.let(uniques::add)
                }
            }
        }

        class Join(t: Any) : Select.Join(t) {
            override fun build(p: Parameters): String {
                p.isManuallyJoined = true
                p.columnFragments += buildFieldFragment(t)
                p.manualJoinFragments += "JOIN ${buildTableFragment(t)}"
                return ""
            }

            class On(t: Any) : Select.Join.On(t) {
                override fun build(p: Parameters): String {
                    val uid = UidBuilder.build(t as KClass<*>)
                    val alias = AliasBuilder.build(uid)
                    p.manualJoinFragments += "ON ${alias}.id"
                    return ""
                }

                class Eq(t: Any) : Select.Join.On.Eq(t) {
                    override fun build(p: Parameters): String {
                        val ref = t as KProperty1<*, *>
                        val uid = UidBuilder.build(ref.javaField?.declaringClass?.kotlin ?: Any::class)
                        val alias = AliasBuilder.build(uid)
                        p.manualJoinFragments += "= $alias.${ref.name.toLowerCase()}"
                        return ""
                    }

                }
            }

            class Where(t: Any) : Select.Join.Where(t) {
                override fun build(p: Parameters): String = buildWherePart(p, this)

                class Eq(t: Any) : Select.Join.Where.Eq(t) {
                    override fun build(p: Parameters): String = buildWhereEqPart(p, this)

                    class Where(t: Any) : Select.Join.Where.Eq.Where(t) {
                        override fun build(p: Parameters): String = buildWherePart(p, this)

                        class Eq(t: Any) : Select.Join.Where.Eq.Where.Eq(t) {
                            override fun build(p: Parameters): String = buildWhereEqPart(p, this)
                        }
                    }
                }
            }
        }

        class Where(t: Any) : Select.Where(t) {
            override fun build(p: Parameters): String = buildWherePart(p, this)

            class Eq(t: Any) : Select.Where.Eq(t) {
                override fun build(p: Parameters): String = buildWhereEqPart(p, this)
            }
        }
    }

    class Update(t: Any) : Sql.Update(t) {
        override fun build(p: Parameters): String {
            // "UPDATE $uid $alias SET firstName = 'newFirstName', lastName = 'newLastName' WHERE $alias.firstName = 'firstName'"
            val uid = UidBuilder.buildUniqueId(t)
            val alias = AliasBuilder.build(uid)
            val stringBuilder = StringBuilder() + "UPDATE " + uid + " $alias" + " SET " +
                    p.updateFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" }

            if (p.whereFragments.isNotEmpty()) {
                stringBuilder + " WHERE " +
                        p.whereFragments.chunked(2).joinToString(", ") { (a, b) -> "$a$b" }
            }
            return stringBuilder.toString()

        }

        class Set(t: Any) : Update.Set(t) {
            override fun build(p: Parameters): String = buildUpdateSet(p, this)

            class Eq(t: Any) : Update.Set.Eq(t) {
                override fun build(p: Parameters): String = buildUpdateSetEq(p, this)

                class And(t: Any) : Update.Set.Eq.And(t) {
                    override fun build(p: Parameters): String = buildUpdateSet(p, this)

                    class Eq(t: Any) : Update.Set.Eq.And.Eq(t) {
                        override fun build(p: Parameters): String = buildUpdateSetEq(p, this)

                        class Where(t: Any) : Update.Set.Eq.And.Eq.Where(t) {
                            override fun build(p: Parameters): String = buildWherePart(p, this)

                            class Eq(t: Any) : Update.Set.Eq.And.Eq.Where.Eq(t) {
                                override fun build(p: Parameters): String = buildWhereEqPart(p, this)
                            }
                        }
                    }
                }

                class Where(t: Any) : Update.Set.Eq.Where(t) {
                    override fun build(p: Parameters): String = buildWherePart(p, this)

                    class Eq(t: Any) : Update.Set.Eq.Where.Eq(t) {
                        override fun build(p: Parameters): String = buildWhereEqPart(p, this)
                    }
                }
            }
        }
    }

    companion object {

        private fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
            val uid = UidBuilder.buildUniqueId(t)
            val alias = AliasBuilder.build(uid)
            return uid to alias
        }

        private fun <T : Any> getFieldValue(field: Field, type: T): Any? {
            field.isAccessible = true
            return field.get(type)
        }

        private fun buildFieldPart(type: Any): String {
            val (_, alias) = buildUidAndAlias(type)

            // FIXME 300
            // println("includedTypes contains:: " + includedTypes.any { it.contains("Int", ignoreCase = true) })

            return type::class
                    .declaredMemberProperties
                    .mapNotNull { it.javaField }
                    .filter(::isCustomField)
                    .joinToString(", ") { "${alias}.${it.name}" }
        }

        private fun isCustomField(field: Field): Boolean {
            return includedTypes.any { isFieldTypeName(field, it) } &&
                    !excludedTypes.any { isFieldTypeName(field, it) }
        }

        private fun isFieldTypeName(field: Field, it: String) =
                field.type.typeName.contains(it, ignoreCase = true)

        private val primitiveTypes = listOf("int", "long")
        private val excludedTypes = listOf("util.", "collection")
        private val includedTypes = listOf("kotlin.", "java.", *(primitiveTypes.toTypedArray()))

        private fun buildJoinsForNaturalRefs(parameters: Sql.Parameters): String {
            return parameters
                    .joinsMap
                    .toOpt()
                    .xmap { map { buildJoinsOnNaturalRefs(it) }.joinToString(" ") }
                    .filter(::hasContent)
                    .getOrElse { "" }
        }

        /*
        val expected = "SELECT $p.address, $p.rentInCents, $a.fullAddress" +
            " FROM $placeUid $p" +
            " JOIN $placeUid$addressUid $joinTableAlias ON $joinTableAlias.${placeUid}refid = $p.id" +
            " JOIN $addressUid $a ON ${joinTableAlias}.${addressUid}refid = $a.id"
         */
        private fun buildJoinsOnNaturalRefs(parentKeyChildValues: Map.Entry<Any, Any>): String {
            val (parentUid, parentAlias) = buildUidAndAlias(parentKeyChildValues.key)
            return (parentKeyChildValues.value as? List<*>).toOpt()
                    .lmap { child: Any -> buildNaturalRefForParent(child, parentUid, parentAlias) }
                    .xmap { joinToString(" ") }
                    .getOrElse { "" }
        }

        private fun buildNaturalRefForParent(t: Any, parentUid: String, parentAlias: String): String {

            return t.toOpt()
                    .mapTo(List::class)
                    .map { it.first() }
                    .ifEmpty { t }
                    .map { element ->
                        val (childUid, childAlias) = buildUidAndAlias(element)
                        val joinTableAlias = AliasBuilder.build("$parentUid$childUid")
                        "JOIN $parentUid$childUid $joinTableAlias ON $joinTableAlias.${parentUid}refid = $parentAlias.id" +
                                " JOIN $childUid $childAlias ON $joinTableAlias.${childUid}refid = $childAlias.id"
                    }
                    .getOrElse { "" }
        }

        private fun buildWherePart(p: Sql.Parameters): String {
            val whereStr = " WHERE "
            val whereFragmentsSize = p.whereFragments.size
            val whereElementCount = 2 // TOIMPROVE: add(Where(t)) add(Eq(t)) -> count == 2, distinctBy(t::class)?
            return when {
                whereFragmentsSize == whereElementCount -> whereStr + p.whereFragments.joinToString("")
                whereFragmentsSize / whereElementCount > 1 -> {
                    whereStr + p.whereFragments
                            .chunked(2)
                            .joinToString(" AND ") { (i, j) -> "$i$j" }
                }
                else -> ""
            }
        }

        private fun buildJoinPart(p: Sql.Parameters): String =
                if (p.joinFragments.isNotEmpty()) " ${p.joinFragments.joinToString(" ")}" else ""

        private fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()

        private fun buildFieldFragment(type: Any): String {
            val (_, alias) = buildUidAndAlias(type)
            return type::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name.toLowerCase()}" }
        }

        private fun buildTableFragment(type: Any): String {
            val (uid, alias) = buildUidAndAlias(type)
            return "$uid $alias"
        }

        private fun buildWherePart(p: Sql.Parameters, sql: Sql): String {
            val kProperty1 = of(sql.t).mapTo(KProperty1::class)

            val alias = kProperty1
                    .map { it.javaField?.declaringClass?.kotlin }
                    .map(UidBuilder::build)
                    .map(AliasBuilder::build)

            p.whereFragments += kProperty1
                    .mapWith(alias) { property, alias1 -> "${alias1}.${property.name}" }
                    .toString()

            return ""
        }

        private fun buildWhereEqPart(p: Sql.Parameters, sql: Sql): String {
            p.whereFragments += of(sql.t).map { " = '$it'" }.toString()
            return ""
        }

        private fun buildUpdateSet(p: Sql.Parameters, sql: Sql): String {
            p.updateFragments += of(sql.t)
                    .mapTo(KProperty1::class)
                    .map { it.name }
                    .get()
                    .toString()
            return ""
        }

        private fun buildUpdateSetEq(p: Sql.Parameters, sql: Sql): String {
            p.updateFragments += of(sql.t).map { " = '$it'" }.toString()
            return ""
        }
    }
}
