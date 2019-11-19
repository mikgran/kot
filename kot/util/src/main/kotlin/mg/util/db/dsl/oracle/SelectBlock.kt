package mg.util.db.dsl.oracle

import mg.util.db.dsl.mysql.BuildingBlock
import mg.util.db.dsl.mysql.SelectBlock as MySqlSelectBlock

class SelectBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlSelectBlock<T>(blocks, type)