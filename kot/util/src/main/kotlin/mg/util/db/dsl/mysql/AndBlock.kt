package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock

class AndBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : WhereBlock<T>(blocks, type) {
    override fun getSqlKeyWord(): String = " AND "
}
