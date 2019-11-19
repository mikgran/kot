package mg.util.db.dsl.oracle

import mg.util.db.dsl.mysql.BuildingBlock
import mg.util.db.dsl.mysql.ValueBlock as MySqlValueBlock

class ValueBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlValueBlock<T>(blocks, type)