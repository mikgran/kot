@file:Suppress("unused")

package mg.util.db.dsl

// A Simple data holding class that holds a list
// of parameters for Dsl to Sql building.
sealed class Sql(val t: Any) {

    data class  Parameters(
            var action: Sql? = null,
            val joins: MutableList<Sql> = mutableListOf(),
            val updates: MutableList<Sql> = mutableListOf(),
            val wheres: MutableList<Sql> = mutableListOf(),
            val columnFragments: MutableList<String> = mutableListOf(),
            val tableFragments: MutableList<String> = mutableListOf(),
            val joinFragments: MutableList<String> = mutableListOf(),
            val updateFragments: MutableList<String> = mutableListOf(),
            val whereFragments: MutableList<String> = mutableListOf(),
            val joinTypes: MutableList<Any> = mutableListOf(),
            val joinsMap: MutableMap<Any, Any> = mutableMapOf()
    ) {
        override fun toString(): String {
            // add some clarity to the bulky data class:
            return StringBuilder("\nParameters:").append(
                    "\naction=${action}" +
                            "\njoins=${joins}" +
                            "\nupdates=${updates}" +
                            "\nwheres=${wheres}" +
                            "\ncolumnFragments=${columnFragments}" +
                            "\ntableFragments=${tableFragments}" +
                            "\njoinFragments=${joinFragments}" +
                            "\nupdateFragments=${updateFragments}" +
                            "\nwhereFragments=${whereFragments}" +
                            "\njoinTypes=${joinTypes}" +
                            "\njoinsMap=${joinsMap}"
            ).toString()
        }
    }

    fun parameters() = parameters
    open fun build(p: Parameters) = ""

    protected var parameters: Parameters = Parameters()

    protected fun <T : Sql> add(type: T): T {

        if (type != this) {
            type.parameters = this.parameters
        }

        when (type) {
            is ShowColumns,
            is Create,
            is Insert,
            is Drop,
            is Delete,
            is Select,
            is Update -> parameters.action = type
            is Select.Join,
            is Select.Join.On,
            is Select.Join.On.Eq -> parameters.joins.add(type)
            is Select.Join.Where,
            is Select.Join.Where.Eq,
            is Select.Where,
            is Select.Where.Eq,
            is Delete.Where,
            is Delete.Where.Eq,
            is Update.Set.Eq.Where,
            is Update.Set.Eq.Where.Eq,
            is Update.Set.Eq.And.Eq.Where,
            is Update.Set.Eq.And.Eq.Where.Eq -> parameters.wheres.add(type)
            is Update.Set,
            is Update.Set.Eq,
            is Update.Set.Eq.And,
            is Update.Set.Eq.And.Eq -> parameters.updates.add(type)
            // TODO: 1 coverage
        }
        return type
    }

    companion object {
        infix fun select(t: Any) = add(Select(t))
        infix fun update(t: Any) = add(Update(t))
        infix fun create(t: Any) = add(Create(t))
        infix fun drop(t: Any) = add(Drop(t))
        infix fun insert(t: Any) = add(Insert(t))
        infix fun delete(t: Any) = add(Delete(t))
        internal infix fun showColumns(t: Any) = add(ShowColumns(t))

        protected fun <T : Sql> add(type: T): T {
            type.add(type)
            return type
        }
    }

    // Execution tree, only allowed commands in context thing
    open class Select(t: Any) : Sql(t) {

        infix fun join(t: Any) = add(Join(t))
        open class Join(t: Any) : Sql(t) {

            infix fun on(t: Any) = add(On(t))
            open class On(t: Any) : Sql(t) {

                infix fun eq(t: Any) = add(Eq(t))
                open class Eq(t: Any) : Sql(t) {

                    infix fun join(t: Any) = add(Join(t)) // loop back to Sql.Select.Join
                }
            }

            infix fun where(t: Any) = add(Where(t))
            open class Where(t: Any) : Sql(t) {

                infix fun eq(t: Any) = add(Eq(t))
                open class Eq(t: Any) : Sql(t) {

                    infix fun and(t: Any) = add(Where(t))
                    open class Where(t: Any) : Sql(t) {

                        infix fun eq(t: Any) = add(Eq(t))
                        open class Eq(t: Any) : Sql(t)
                    }
                }
            }
        }

        infix fun where(t: Any) = add(Where(t))
        open class Where(t: Any) : Sql(t) {

            infix fun eq(t: Any) = add(Eq(t))
            open class Eq(t: Any) : Sql(t) {

                infix fun and(t: Any) = add(Where(t))
            }
        }
    }

    open class Update(t: Any) : Sql(t) {

        infix fun set(t: Any) = add(Set(t))
        open class Set(t: Any) : Sql(t) {

            infix fun eq(t: Any) = add(Eq(t))
            open class Eq(t: Any) : Sql(t) {

                infix fun where(t: Any) = add(Where(t))
                open class Where(t: Any) : Sql(t) {

                    infix fun eq(t: Any) = add(Eq(t))
                    open class Eq(t: Any) : Sql(t)
                }

                infix fun and(t: Any) = add(And(t))
                open class And(t: Any) : Sql(t) {

                    infix fun eq(t: Any) = add(Eq(t))
                    open class Eq(t: Any) : Sql(t) {

                        infix fun where(t: Any) = add(Where(t))
                        open class Where(t: Any) : Sql(t) {

                            infix fun eq(t: Any) = add(Eq(t))
                            open class Eq(t: Any) : Sql(t)
                        }
                    }
                }
            }
        }
    }

    open class ShowColumns(t: Any) : Sql(t)
    open class Create(t: Any) : Sql(t)
    open class Insert(t: Any) : Sql(t)
    open class Drop(t: Any) : Sql(t)

    open class Delete(t: Any) : Sql(t) {

        infix fun where(t: Any) = add(Where(t))
        open class Where(t: Any) : Sql(t) {

            infix fun eq(t: Any) = add(Eq(t))
            open class Eq(t: Any) : Sql(t) {

                infix fun and(t: Any) = add(Where(t)) // loop back to Sql.Delete.Where
            }
        }
    }
}