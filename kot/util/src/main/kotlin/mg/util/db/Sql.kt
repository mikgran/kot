package mg.util.db

import kotlin.reflect.KProperty1

// Class chains intended just to collect information and provide
// clear options when sql syntax is applicable
class Sql {
    companion object {

        infix fun <T> select(t: T): SelectBlock<T> = newListAndCacheBlock { list -> SelectBlock(list, t) }
        infix fun <T> update(t: T): UpdateBlock<T> = newListAndCacheBlock { list -> UpdateBlock(list, t) }

        private fun <T : BuildingBlock> newListAndCacheBlock(funktion: (blocks: MutableList<BuildingBlock>) -> T): T {
            val list = mutableListOf<BuildingBlock>()
            val buildingBlock = funktion(list)
            list.add(buildingBlock)
            return buildingBlock
        }
    }
}

abstract class BuildingBlock {
    abstract val blocks: MutableList<BuildingBlock>
    fun list() = blocks
    protected fun simpleName() = this::class.simpleName
}

data class SelectBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheWhereBlock(blocks, type)
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }
}

data class WhereBlock<T : KProperty1<*, *>>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T> eq(type: T): OperationBlock<T> {
        val operationBlock = OperationBlock(blocks, type)
        blocks.add(operationBlock)
        return operationBlock
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }
}

data class OperationBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }
}

// TODO: handle updates
data class UpdateBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheWhereBlock(blocks, type)
    }


}

private fun <T : KProperty1<*, *>> getAndCacheWhereBlock(blocks: MutableList<BuildingBlock>, type: T): WhereBlock<T> {
    val whereBlock = WhereBlock(blocks, type)
    blocks.add(WhereBlock(blocks, type))
    return whereBlock
}