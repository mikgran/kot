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

    override fun build(dp: DslParameters): String {
        return " '${type.toString()}'"
    }
    override fun buildFields(dp: DslParameters): String = ""
}