package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters

class DropBlock<T: Any>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock(type) {

    override fun buildDrop(dp: DslParameters): String {
        return "DROP TABLE IF EXISTS ${dp.uniqueId}"
    }
}
