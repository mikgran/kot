package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.ValueBlock as MySqlValueBlock

class ValueBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T, override val operation: String) : MySqlValueBlock<T>(blocks, type, operation)