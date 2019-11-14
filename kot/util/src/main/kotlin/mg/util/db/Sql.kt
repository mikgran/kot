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
        return getAndCacheWhereBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }
}

data class WhereBlock<T : KProperty1<*, *>>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T> eq(type: T): OperationBlock<T> {
        return getAndCacheWhereBlock(type, blocks) { t, b -> OperationBlock(b, t) }
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
        return getAndCacheWhereBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }
}

private fun <T, R: BuildingBlock> getAndCacheWhereBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
    val block = f(type, list)
    list.add(block)
    return block
}
