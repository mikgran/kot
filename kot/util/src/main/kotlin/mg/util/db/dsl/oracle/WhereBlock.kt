package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.WhereBlock as MySqlWhereBlock

class WhereBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlWhereBlock<T>(blocks, type) {

    override fun <T : Any> newValue(blocks: MutableList<BuildingBlock>, type: T): ValueBlock<T> = ValueBlock(blocks, type)
}