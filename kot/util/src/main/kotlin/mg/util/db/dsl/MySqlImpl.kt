package mg.util.db.dsl

import mg.util.common.plus
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

class MySqlImpl {

    class Create(t: Any) : Sql.Create(t) {
        // TODO: 1 does not include multilayer creates yet
        override fun build(p: Parameters): String {
            return MySqlCreateBuilder().build(p, this)
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
            return MySqlInsertBuilder().build(p, this)
        }
    }

    class Delete(t: Any) : Sql.Delete(t) {
        override fun build(p: Parameters): String {
            // TODO 1: no multi table deletes supported? yes/no/abandon
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

    // FIXME: 104 support SELECT composition and manual
    class Select(t: Any) : Sql.Select(t) {
        override fun build(p: Parameters): String {
            return MySqlSelectBuilder().build(p, this)
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
                        p.manualJoinFragments += "= $alias.${ref.name.lowercase()}"
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
        // TODO: 1 recursive multilayer update -> save all dirty objects? / abandon idea?
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

        fun <T : Any> buildUidAndAlias(t: T): Pair<String, String> {
            val uid = UidBuilder.buildUniqueId(t)
            val alias = AliasBuilder.build(uid)
            return uid to alias
        }

        fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()

        private fun buildFieldFragment(type: Any): String {
            val (_, alias) = buildUidAndAlias(type)
            return type::class.declaredMemberProperties.joinToString(", ") { "${alias}.${it.name.lowercase()}" }
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
