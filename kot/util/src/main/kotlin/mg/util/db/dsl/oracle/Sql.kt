package mg.util.db.dsl.oracle

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.mysql.Sql as Mysql

class Sql : Mysql() {
    override fun <T : Any> newSelect(list: MutableList<BuildingBlock>, t: T): SelectBlock<T> = SelectBlock(list, t)
    override fun <T : Any> newUpdate(list: MutableList<BuildingBlock>, t: T): UpdateBlock<T> = UpdateBlock(list, t)
}