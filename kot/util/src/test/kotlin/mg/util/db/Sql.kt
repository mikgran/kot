package mg.util.db

class Sql(val b: BuildingBlock) {

    val buildChain = mutableListOf<BuildingBlock>()

    init {
        buildChain.add(b)
    }

    infix fun where(s: String) : Sql {
        buildChain.add(WhereBlock(s))
        return this
    }

    companion object {
        infix fun <T> select(t: T): Sql = Sql(SelectBlock(t))
        infix fun <T, V> select(p: Pair<T, V>) = Sql(SelectBlock())
    }
}

sealed class BuildingBlock
data class SelectBlock<T>(val type: T) : BuildingBlock()
data class WhereBlock(val where: String) : BuildingBlock()
data class CompareBlock(val eq: String) : BuildingBlock()