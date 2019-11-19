package mg.util.db.dsl.oracle

import mg.util.db.dsl.mysql.BuildingBlock
import mg.util.db.dsl.mysql.InnerJoinBlock as MySqlInnerJoinBlock

class InnerJoinBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlInnerJoinBlock<T>(blocks, type)