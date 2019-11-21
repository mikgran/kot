package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters

open class ValueBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {
    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    infix fun <T: Any> and(type: T): AndBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> AndBlock(b, t) }
    }

    override fun buildSelect(dp: DslParameters): String = " '$type'"
    override fun buildFields(dp: DslParameters): String = ""
}