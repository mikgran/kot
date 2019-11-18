package mg.util.db.dsl

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