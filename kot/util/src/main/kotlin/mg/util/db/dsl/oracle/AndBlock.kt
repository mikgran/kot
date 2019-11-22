package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.AndBlock as MysqlAnd

class AndBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MysqlAnd<T>(blocks, type)