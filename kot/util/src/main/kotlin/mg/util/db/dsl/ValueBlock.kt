package mg.util.db.dsl

data class ValueBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }
}