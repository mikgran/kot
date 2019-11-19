package mg.util.db.dsl.mysql

import mg.util.db.Config
import mg.util.db.DBConfig
import mg.util.db.DBO
import mg.util.db.SqlMapperFactory

abstract class BuildingBlock {
    abstract val blocks: MutableList<BuildingBlock>
    fun list() = blocks
    protected fun simpleName() = this::class.simpleName
    abstract fun build(dp: DslParameters): String
    abstract fun buildFields(dp: DslParameters) : String
    private val dbConfig = DBConfig(Config())
    internal val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))

    fun <T : Any, R : BuildingBlock> getAndCacheBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
        val block = f(type, list)
        list.add(block)
        return block
    }
}