package mg.util.db.dsl

// A Simple data holding class that holds a list
// of parameters for Dsl to Sql building.
sealed class Sql(val t: Any) {

    data class Parameters(
            var action: Sql? = null,
            val joins: MutableList<Sql> = mutableListOf(),
            val updates: MutableList<Sql> = mutableListOf(),
            val wheres: MutableList<Sql> = mutableListOf(),
            val fieldFragments: MutableList<String> = mutableListOf(),
            val tableFragments: MutableList<String> = mutableListOf(),
            val joinFragments: MutableList<String> = mutableListOf(),
            val updateFragments: MutableList<String> = mutableListOf(),
            val whereFragments: MutableList<String> = mutableListOf(),
            val joinTypes: MutableList<Any> = mutableListOf()
    )

    fun parameters() = parameters!!

    protected var parameters: Parameters? = null

    protected fun <T : Sql> add(type: T): T {

        if (type != this) {
            type.parameters = this.parameters
        }

        when (type) {
            is Create,
            is Insert,
            is Drop,
            is Delete,
            is Select,
            is Update -> parameters?.action = type
            is Select.Join -> parameters?.joins?.add(type)
            is Select.Join.Where,
            is Select.Join.Where.Eq,
            is Select.Where,
            is Select.Where.Eq,
            is Update.Set.Eq.Where,
            is Update.Set.Eq.Where.Eq,
            is Update.Set.Eq.And.Eq.Where,
            is Update.Set.Eq.And.Eq.Where.Eq -> parameters?.wheres?.add(type)
            is Update.Set,
            is Update.Set.Eq,
            is Update.Set.Eq.And,
            is Update.Set.Eq.And.Eq -> parameters?.updates?.add(type)
            // TODO: -15 coverage
        }
        return type
    }

    companion object {
        infix fun select(t: Any) = Select(t).also { it.parameters = Parameters(); it.add(it) }
        infix fun update(t: Any) = Update(t).also { it.parameters = Parameters(); it.add(it) }
        infix fun create(t: Any) = Create(t).also { it.parameters = Parameters(); it.add(it) }
        infix fun drop(t: Any) = Drop(t).also { it.parameters = Parameters(); it.add(it) }
        infix fun insert(t: Any) = Insert(t).also { it.parameters = Parameters(); it.add(it) }
    }

    // val sql = SQL2 select Person() where Person::firstName eq "name"
    // private val a = "SELECT p.firstName, p.lastName FROM Person AS p WHERE p.firstName = 'name'"

    class Select(t: Any) : Sql(t) {

        infix fun join(t: Any) = add(Join(t))
        class Join(t: Any) : Sql(t) {

            infix fun on(t: Any) = add(On(t))
            class On(t: Any) : Sql(t) {
                infix fun eq(t: Any) = add(Eq(t))
                class Eq(t: Any) : Sql(t) {

                }
            }

            infix fun where(t: Any) = add(Where(t)) // TODO: 49 test all unused functions
            class Where(t: Any) : Sql(t) {

                infix fun eq(t: Any) = add(Eq(t))
                class Eq(t: Any) : Sql(t) {

                    infix fun and(t: Any) = add(Where(t))
                    class Where(t: Any) : Sql(t) {

                        infix fun eq(t: Any) = add(Eq(t))
                        class Eq(t: Any) : Sql(t)
                    }
                }
            }
        }

        infix fun where(t: Any) = add(Where(t))
        class Where(t: Any) : Sql(t) {

            infix fun eq(t: Any) = add(Eq(t))
            class Eq(t: Any) : Sql(t) {

                infix fun and(t: Any) = add(Where(t))
            }
        }
    }

    class Update(t: Any) : Sql(t) {

        infix fun set(t: Any) = add(Set(t))
        class Set(t: Any) : Sql(t) {

            infix fun eq(t: Any) = add(Eq(t))
            class Eq(t: Any) : Sql(t) {

                infix fun where(t: Any) = add(Where(t))
                class Where(t: Any) : Sql(t) {

                    infix fun eq(t: Any) = add(Eq(t))
                    class Eq(t: Any) : Sql(t)
                }

                infix fun and(t: Any) = add(And(t))
                class And(t: Any) : Sql(t) {

                    infix fun eq(t: Any) = add(Eq(t))
                    class Eq(t: Any) : Sql(t) {

                        infix fun where(t: Any) = add(Where(t))
                        class Where(t: Any) : Sql(t) {

                            infix fun eq(t: Any) = add(Eq(t))
                            class Eq(t: Any) : Sql(t)
                        }
                    }
                }
            }
        }
    }

    class Create(t: Any) : Sql(t)
    class Insert(t: Any) : Sql(t)
    class Drop(t: Any) : Sql(t)
    class Delete(t: Any) : Sql(t)
}