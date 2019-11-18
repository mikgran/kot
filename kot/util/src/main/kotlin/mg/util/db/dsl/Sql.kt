package mg.util.db.dsl

class Sql {
    companion object {

        infix fun <T : Any> select(t: T): SelectBlock<T> = newListAndCacheBlock { list -> SelectBlock(list, t) }
        infix fun <T : Any> update(t: T): UpdateBlock<T> = newListAndCacheBlock { list -> UpdateBlock(list, t) }

        private fun <T : BuildingBlock> newListAndCacheBlock(funktion: (blocks: MutableList<BuildingBlock>) -> T): T {
            val list = mutableListOf<BuildingBlock>()
            val buildingBlock = funktion(list)
            list.add(buildingBlock)
            return buildingBlock
        }
    }
}