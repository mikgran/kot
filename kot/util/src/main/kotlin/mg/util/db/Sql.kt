package mg.util.db

import kotlin.reflect.KProperty1

class Sql {
    companion object {
        infix fun <T> select(t: T): SelectBlock<T> {
            val buildingBlocks = mutableListOf<BuildingBlock>()
            val selectBlock = SelectBlock(buildingBlocks, t)
            buildingBlocks.add(selectBlock)
            return selectBlock
        }
    }
}

abstract class BuildingBlock {
    abstract val blocks: MutableList<BuildingBlock>
    fun list() = blocks
    protected fun simpleName() = this::class.simpleName
    fun build(): String = SqlDslMapper.map(blocks)
}

data class SelectBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        val whereBlock = WhereBlock(blocks, type)
        blocks.add(WhereBlock(blocks, type))
        return whereBlock
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

}