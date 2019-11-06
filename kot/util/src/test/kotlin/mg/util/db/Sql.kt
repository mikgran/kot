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
}
data class SelectBlock<T>(val bc: MutableList<BuildingBlock>, val t: T) : BuildingBlock() {
    infix fun <T : KProperty1<*, *>> where(t: T): WhereBlock<T> {
        val whereBlock = WhereBlock(bc, t)
        bc.add(WhereBlock(bc, t))
        return whereBlock
    }

    override fun toString(): String {
        return "SelectBlock(t=$t)"
    }
}

data class WhereBlock<T : KProperty1<*, *>>(val bc: MutableList<BuildingBlock>, val t: T) : BuildingBlock() {
    infix fun <T> eq(t: T): OperationBlock<T> {
        val operationBlock = OperationBlock(bc, t)
        bc.add(operationBlock)
        return operationBlock
    }

    override fun toString(): String {
        return "WhereBlock(t=$t)"
    }
}

data class OperationBlock<T>(val bc: MutableList<BuildingBlock>, val t: T) : BuildingBlock() {
    fun getList() = bc

    override fun toString(): String {
        return "OperationBlock(t=$t)"
    }
}