package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.UpdateBlock as MySqlUpdateBlock

class UpdateBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlUpdateBlock<T>(blocks, type)