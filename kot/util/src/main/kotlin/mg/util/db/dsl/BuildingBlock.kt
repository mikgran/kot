package mg.util.db.dsl

import mg.util.common.Common
import mg.util.db.*
import mg.util.functional.Opt2

abstract class BuildingBlock(protected val t: Any) {
    abstract val blocks: MutableList<BuildingBlock>
    fun list() = blocks
    protected fun simpleName() = this::class.simpleName
    open fun buildCreate(dp: DslParameters): String = ""
    open fun buildDrop(dp: DslParameters): String = ""
    open fun buildSelect(dp: DslParameters): String = "" // do as last always
    open fun buildFields(dp: DslParameters): String { // do as first always
        dp.typeT = Opt2.of(t)
                .getOrElseThrow { Exception("buildFields: Missing select type") }!!

        dp.uniqueId = Opt2.of(dp.typeT)
                .map(UidBuilder::buildUniqueId)
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $t") }!!

        dp.uniqueIdAlias = AliasBuilder.build(dp.uniqueId!!)
        return ""
    }
    // buildDelete
    // buildTruncate

    private val dbConfig = DBConfig(Config())
    internal val dbo = DBO(SqlMapperFactory.get(dbConfig.mapper))

    fun <T : Any, R : BuildingBlock> getAndCacheBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
        val block = f(type, list)
        list.add(block)
        return block
    }
}