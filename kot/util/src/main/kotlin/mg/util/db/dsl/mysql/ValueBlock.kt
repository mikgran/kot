package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import kotlin.reflect.KProperty1

open class ValueBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T, open val operation: String) : BuildingBlock(type) {

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    infix fun <T: Any> and(type: T): AndBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> AndBlock(b, t) }
    }

    infix fun <T: KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t)}
    }
    override fun buildSelect(dp: DslParameters): String = " $operation '$type'"
    override fun buildFields(dp: DslParameters): String = ""
}