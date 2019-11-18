package mg.util.db.dsl

import kotlin.reflect.KProperty1

data class SelectBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }
}