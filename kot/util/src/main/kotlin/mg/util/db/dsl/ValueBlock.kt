package mg.util.db.dsl

data class ValueBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {
    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    override fun build(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}