package mg.util.db.dsl

data class InnerJoinBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {
    infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun build(dp: DslParameters): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun buildFields(dp: DslParameters): String = ""
}

