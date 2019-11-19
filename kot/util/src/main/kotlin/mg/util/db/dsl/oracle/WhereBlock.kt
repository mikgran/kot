package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.WhereBlock as MySqlWhereBlock

class WhereBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlWhereBlock<T>(blocks, type) {

    override infix fun <T : Any> eq(type: T): ValueBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> ValueBlock(b, t) }
    }
}