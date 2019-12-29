package mg.util.db.dsl

import mg.util.common.Common
import mg.util.db.*
import mg.util.functional.Opt2.Factory.of

abstract class BuildingBlock(val t: Any) {

    open val blocks: MutableList<BuildingBlock> = mutableListOf()

    open fun buildCreate(dp: DslParameters): String = ""
    open fun buildDrop(dp: DslParameters): String = ""
    open fun buildInsert(dp: DslParameters): String = ""
    open fun buildDelete(dp: DslParameters): String = ""
    open fun buildUpdate(dp: DslParameters): String = ""
    open fun buildSelect(dp: DslParameters): String = "" // do as last always
    open fun buildFields(dp: DslParameters): String = "" // do as first always
    fun list() = blocks
    internal fun simpleName() = this::class.simpleName
    // buildDelete
    // buildTruncate

    open fun buildDslParameters(): DslParameters {
        val dp = DslParameters()
        dp.typeT = of(this.t)
                .getOrElseThrow { Exception("buildFields: Missing select type") }!!

        dp.uniqueId = of(dp.typeT)
                .map(UidBuilder::buildUniqueId)
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $t") }!!

        dp.uniqueIdAlias = AliasBuilder.build(dp.uniqueId!!)
        return dp
    }

    fun <T : Any, R : BuildingBlock> getAndCacheBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
        val block = f(type, list)
        list.add(block)
        return block
    }
}

sealed class SQL2(t: Any) : BuildingBlock(t) {

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

