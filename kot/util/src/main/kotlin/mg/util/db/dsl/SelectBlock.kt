package mg.util.db.dsl

data class SelectBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T : Any> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun build(): String {


        return ""
    }
}