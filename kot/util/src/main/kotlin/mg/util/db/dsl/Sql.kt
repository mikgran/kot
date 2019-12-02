package mg.util.db.dsl

import mg.util.db.dsl.mysql.*

abstract class Sql {

    open fun <T : BuildingBlock> newCachedBlock(mapper: (blocks: MutableList<BuildingBlock>) -> T): T {
        val list = mutableListOf<BuildingBlock>()
        val buildingBlock = mapper(list)
        list.add(buildingBlock)
        return buildingBlock
    }

    open fun <T : Any> newSelect(list: MutableList<BuildingBlock>, t: T) = SelectBlock(list, t)
    open fun <T : Any> newUpdate(list: MutableList<BuildingBlock>, t: T) = UpdateBlock(list, t)
    open fun <T : Any> newCreate(list: MutableList<BuildingBlock>, t: T) = CreateBlock(list, t)
    open fun <T : Any> newDrop(list: MutableList<BuildingBlock>, t: T) = DropBlock(list, t)
    open fun <T : Any> newInsert(list: MutableList<BuildingBlock>, t: T) = InsertBlock(list, t)

    open infix fun <T : Any> select(t: T): SelectBlock<T> = newCachedBlock { list -> newSelect(list, t) }
    open infix fun <T : Any> update(t: T): UpdateBlock<T> = newCachedBlock { list -> newUpdate(list, t) }
    open infix fun <T : Any> create(t: T): CreateBlock<T> = newCachedBlock { list -> newCreate(list, t) }
    open infix fun <T : Any> drop(t: T): DropBlock<T> = newCachedBlock { list -> newDrop(list, t) }
    open infix fun <T : Any> insert(t: T): InsertBlock<T> = newCachedBlock { list -> newInsert(list, t) }


}
