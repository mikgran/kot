package mg.util.db.dsl

data class WhereBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {
    infix fun <T : Any> eq(type: T): ValueBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> ValueBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun build(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}