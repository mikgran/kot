package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock

open class Sql {

    open fun <T : Any> newSelect(list: MutableList<BuildingBlock>, t: T) = SelectBlock(list, t)
    open fun <T : Any> newUpdate(list: MutableList<BuildingBlock>, t: T) = UpdateBlock(list, t)

    open infix fun <T : Any> select(t: T): SelectBlock<T> = newCachedBlock { list -> newSelect(list, t) }
    open infix fun <T : Any> update(t: T): UpdateBlock<T> = newCachedBlock { list -> newUpdate(list, t) }

    private fun <T : BuildingBlock> newCachedBlock(mapper: (blocks: MutableList<BuildingBlock>) -> T): T {
        val list = mutableListOf<BuildingBlock>()
        val buildingBlock = mapper(list)
        list.add(buildingBlock)
        return buildingBlock
    }

}