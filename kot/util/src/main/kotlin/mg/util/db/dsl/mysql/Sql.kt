package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock

open class Sql {

    open infix fun <T : Any> select(t: T): SelectBlock<T> = newListAndCacheBlock { list -> SelectBlock(list, t) }
    open infix fun <T : Any> update(t: T): UpdateBlock<T> = newListAndCacheBlock { list -> UpdateBlock(list, t) }

    open fun <T : BuildingBlock> newListAndCacheBlock(funktion: (blocks: MutableList<BuildingBlock>) -> T): T {
        val list = mutableListOf<BuildingBlock>()
        val buildingBlock = funktion(list)
        list.add(buildingBlock)
        return buildingBlock
    }

    companion object {
        infix fun <T : Any> select(t: T): SelectBlock<T> = Sql().select(t)
        infix fun <T : Any> update(t: T): UpdateBlock<T> = Sql().update(t)
    }
}