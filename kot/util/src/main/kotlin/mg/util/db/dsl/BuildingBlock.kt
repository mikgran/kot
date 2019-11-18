package mg.util.db.dsl

abstract class BuildingBlock {
    abstract val blocks: MutableList<BuildingBlock>
    fun list() = blocks
    protected fun simpleName() = this::class.simpleName
    abstract fun build(): String

    fun <T : Any, R : BuildingBlock> getAndCacheBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
        val block = f(type, list)
        list.add(block)
        return block
    }
}