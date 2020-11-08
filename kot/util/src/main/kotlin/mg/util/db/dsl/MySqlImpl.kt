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

            // - inspect table columns or create table
            // - creates and alters (depth?)
            // p.action

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

    // FIXME: 10 propagation of child items
    class Insert(t: Any) : Sql.Insert(t) {
        override fun build(p: Parameters): String {

            // Complex reference many to many


            // Simple reference one to one
            val dp = DslParameters().apply {
                typeT = t
                uniqueId = UidBuilder.buildUniqueId(t)
                uniqueIdAlias = AliasBuilder.build(uniqueId!!)
            }

            val sqls = mutableListOf<String>()

            sqls += buildInsertSql(t)

            sqls += getFieldsWithCustoms(dp)
                    .map { field -> fieldGet(field, dp.typeT) }
                    .map(this@Insert::buildInsertSql)

            sqls += getFieldsWithListOfCustoms(dp)
                    .map { field -> fieldGet(field, dp.typeT) }
                    .map(this@Insert::buildInsertSql)

            return sqls.joinToString(";")
        }

        private fun buildInsertSql(type: Any): String {

            val padding1 = "INSERT INTO ${UidBuilder.buildUniqueId(type)} ("
            val padding2 = ") VALUES ("
            val padding3 = ")"

            val properties = of(t)
                    .map { it::class.memberProperties.toCollection(ArrayList()) }
                    .lfilter { p: KProperty1<out Any, *> -> !isCustom(p.javaField!!) }

            val fieldsCommaSeparated = of(properties)
                    .map { it.joinToString(", ") { p -> p.name } }

            val fieldsValuesCommaSeparated = of(properties)
                    .map { it.joinToString(", ") { p -> "'${getFieldValueAsString(p, t)}'" } }

            return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"
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

    class Select(t: Any) : Sql.Select(t) {
        override fun build(p: Parameters): String {
            p.tableFragments.add(0, buildTableFragment(t))
            p.joinsMap.putAll(buildJoinsMap(t, p))

            of(buildJoinsForNaturalRefs(p))
                    .filter(String::isNotEmpty)
                    .map(p.joinFragments::add)

            collectUniqueTypesFrom(p.action, p.joinsMap)
                    .forEach { p.columnFragments += buildFieldPart(it) }

            val sb = StringBuilder() +
                    "SELECT ${p.columnFragments.joinToString(", ")}" +
                    " FROM ${p.tableFragments.joinToString(", ")}" +
                    buildWherePart(p) +
                    buildJoinPart(p)
            return sb.toString()
        }

        private fun buildTableFragment(type: Any): String {
            val (uid, alias) = buildUidAndAlias(type)
            return "$uid $alias"
        }

        private fun buildJoinsMap(root: Any, p: Parameters): MutableMap<Any, List<Any>> {
            val joinsMap = mutableMapOf<Any, List<Any>>()
            of(root)
                    .map {
                        val list = linksForParent(it)
                        if (list.isNotEmpty()) {
                            joinsMap[it] = list
                        }
                        list
                    }
                    .xmap { forEach { buildJoinsMap(it, p) } }
            return joinsMap
        }

        private fun linksForParent(type: Any): List<Any> {
            return type::class.java.declaredFields.toList()
                    .toOpt()
                    .lfilter(::isCustom or ::isList)
                    .lxmap<Field, Any> { mapNotNull { getFieldValue(it, type) } }
                    .getOrElse { emptyList() }
        }

        private fun collectUniqueTypesFrom(action: Sql?, joinsMap: MutableMap<*, *>): MutableSet<Any> {
            val uniques = mutableSetOf<Any>()
            action?.t
                    .toOpt()
                    .map(uniques::add)
            joinsMap.iterator()
                    .toOpt()
                    .lmap { entry: MutableMap.MutableEntry<Any, Any> ->
                        uniques += entry.key
                        (entry.value as? List<*>)?.filterNotNull()?.forEach { uniques += it }
                        entry
                    }
            return uniques
        }

        class Join(t: Any) : Select.Join(t) {
            override fun build(p: Parameters): String {
                p.columnFragments += buildFieldFragment(t)
                p.joinFragments += "JOIN ${buildTableFragment(t)}"
                return ""
            }

            class On(t: Any) : Select.Join.On(t) {
                override fun build(p: Parameters): String {
                    val uid = UidBuilder.build(t as KClass<*>)
                    val alias = AliasBuilder.build(uid)
                    p.joinFragments += "ON ${alias}.id"
                    return ""
                }

                class Eq(t: Any) : Select.Join.On.Eq(t) {
                    override fun build(p: Parameters): String {
                        val ref = t as KProperty1<*, *>
                        val uid = UidBuilder.build(ref.javaField?.declaringClass?.kotlin ?: Any::class)
                        val alias = AliasBuilder.build(uid)
                        p.joinFragments += "= $alias.${ref.name}"
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
            return type::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
        }

        private fun buildJoinsForNaturalRefs(p: Sql.Parameters): String {
            return of(p.joinsMap)
                    .xmap { map { buildJoinsOnNaturalRefs(it) }.joinToString(" AND ") }
                    .filter(::hasContent)
                    .map { "JOIN $it" }
                    .filter(String::isNotEmpty)
                    .getOrElse { "" }
        }

        private fun buildJoinsOnNaturalRefs(entry: Map.Entry<Any, Any>): String {
            val (uidRoot, aliasRoot) = buildUidAndAlias(entry.key)
            return of(entry.value as? List<*>)
                    .lmap { type: Any ->
                        val (uidRef, aliasRef) = buildUidAndAlias(type)
                        "$uidRef $aliasRef ON ${aliasRoot}.id = ${aliasRef}.${uidRoot}RefId"
                    }
                    .xmap { joinToString(" AND ") }
                    .getOrElse("")
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
            return type::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name}" }
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
