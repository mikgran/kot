package mg.util.db.dsl

// A Simple data holding class that holds a list
// of parameters for Dsl to Sql building.
sealed class SQL2(val t: Any) {

    data class Parameters(
            var action: SQL2? = null,
            val joins: MutableList<SQL2> = mutableListOf(),
            val wheres: MutableList<SQL2> = mutableListOf()
    )

    fun parameters() = parameters!!

    protected var parameters: Parameters? = null

    protected fun <T : SQL2> add(type: T): T {

        if (type != this) {
            type.parameters = this.parameters
        }

        when (type) {
            is Select -> parameters?.action = type
            is Select.Join -> parameters?.joins?.add(type)
            is Select.Join.Where,
            is Select.Join.Where.Eq,
            is Select.Where,
            is Select.Where.Eq -> parameters?.wheres?.add(type)
            is Update -> parameters?.action = type
        }
        return type
    }

    companion object {
        infix fun select(t: Any) = Select(t).also { it.parameters = Parameters(); it.add(it) }
        infix fun update(t: Any) = Update(t).also { it.parameters = Parameters(); it.add(it) }
    }

    // val sql = SQL2 select Person() where Person::firstName eq "name"
    // private val a = "SELECT p.firstName, p.lastName FROM Person AS p WHERE p.firstName = 'name'"

    class Select(t: Any) : SQL2(t) {

        infix fun join(t: Any) = add(Join(t))
        class Join(t: Any) : SQL2(t) {

            infix fun where(t: Any) = add(Where(t))
            class Where(t: Any) : SQL2(t) {

                infix fun eq(t: Any) = add(Eq(t))
                class Eq(t: Any) : SQL2(t) {

                    infix fun and(t: Any) = add(Where(t))
                }
            }
        }

        infix fun where(t: Any) = add(Where(t))
        class Where(t: Any) : SQL2(t) {

            infix fun eq(t: Any) = add(Eq(t))
            class Eq(t: Any) : SQL2(t) {

                infix fun and(t: Any) = add(Where(t))
            }
        }
    }

    class Update(t: Any) : SQL2(t) {
        infix fun set(t: Any) = add(Set(t))
        class Set(t: Any) : SQL2(t) {

            infix fun where(t: Any) = add(Where(t))
            class Where(t: Any) : SQL2(t) {

                infix fun eq(t: Any) = add(Eq(t))
                class Eq(t: Any) : SQL2(t) {

                    infix fun and(t: Any) = add(Where(t))
                }
            }
        }

        class delete(t: Any) : SQL2(t)


    }


}