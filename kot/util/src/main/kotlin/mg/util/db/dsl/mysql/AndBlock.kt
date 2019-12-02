package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import kotlin.reflect.KProperty1

open class AndBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : WhereBlock<T>(blocks, type) {
    override fun getSqlKeyWord(): String = " AND "
    override fun buildDelete(dp: DslParameters): String {
        return ", ${(type as KProperty1<*, *>).name}"
    }
}
