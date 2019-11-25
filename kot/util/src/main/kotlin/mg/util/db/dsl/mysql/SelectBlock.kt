package mg.util.db.dsl.mysql

import mg.util.common.Common
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.full.memberProperties

open class SelectBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {

    open fun <T: Any> newWhere(type: T, list: MutableList<BuildingBlock>) = WhereBlock(list, type)
    open fun <T: Any> newInnerJoin(type: T, list: MutableList<BuildingBlock>) = InnerJoinBlock(list, type)

    open infix fun <T : Any> where(type: T): WhereBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newWhere(t, b) }
    open infix fun <T : Any> join(type: T): InnerJoinBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newInnerJoin(t, b) }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun buildSelect(dp: DslParameters): String {

        val builder = of(StringBuilder())
                .rcv {
                    append("SELECT ") // SELECT p.firstName, p.lastName FROM Persons p
                    append(dp.fields)
                    append(" FROM ")
                    append(dp.uniqueId)
                    append(" ")
                    append(dp.uniqueIdAlias)
                }

        return builder.get().toString()
    }

    override fun buildFields(dp: DslParameters): String {
        dp.typeT = of(type)
                .getOrElseThrow { Exception("buildFields: Missing select type") }!!

        // TODO 1 of(dp.typeT).map()

        dp.uniqueId = of(dp.typeT)
                .map (::buildUniqueId)
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $type") }!!

        dp.uniqueIdAlias = AliasBuilder.alias(dp.uniqueId!!)

        // "SELECT p.firstName, p.lastName FROM Person p"
        return dp.typeT!!::class.memberProperties.joinToString(", ") { "${dp.uniqueIdAlias}.${it.name}" }
    }
}

