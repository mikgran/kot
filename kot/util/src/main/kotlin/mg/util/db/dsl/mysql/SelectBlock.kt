package mg.util.db.dsl.mysql

import mg.util.common.Common
import mg.util.db.AliasBuilder
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.full.memberProperties

open class SelectBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {
    infix fun <T : Any> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun build(dp: DslParameters): String {

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

        dp.uniqueId = of(dbo)
                .map { it.buildUniqueId(dp.typeT!!) }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $type") }!!

        dp.uniqueIdAlias = AliasBuilder.alias(dp.uniqueId!!)

        // "SELECT p.firstName, p.lastName FROM Person p"
        return dp.typeT!!::class.memberProperties.joinToString(", ") { "${dp.uniqueIdAlias}.${it.name}" }
    }
}

