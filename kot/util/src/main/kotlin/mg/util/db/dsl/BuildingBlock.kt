package mg.util.db.dsl

import mg.util.common.Common
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.functional.Opt2.Factory.of

abstract class BuildingBlock(val t: Any) {

    open val blocks: MutableList<BuildingBlock> = mutableListOf()

    open fun buildCreate(dp: DslParameters): String = ""
    open fun buildDrop(dp: DslParameters): String = ""
    open fun buildInsert(dp: DslParameters): String = ""
    open fun buildDelete(dp: DslParameters): String = ""
    open fun buildUpdate(dp: DslParameters): String = ""
    open fun buildSelect(dp: DslParameters): String = "" // do as last always
    open fun buildFields(dp: DslParameters): String = "" // do as first always
    fun list() = blocks
    internal fun simpleName() = this::class.simpleName
    // buildDelete
    // buildTruncate

    open fun buildDslParameters(): DslParameters {
        val dp = DslParameters()
        dp.typeT = of(this.t)
                .getOrElseThrow { Exception("buildFields: Missing select type") }!!

        dp.uniqueId = of(dp.typeT)
                .map(UidBuilder::buildUniqueId)
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("buildFields: Cannot build uid for $t") }!!

        dp.uniqueIdAlias = AliasBuilder.build(dp.uniqueId!!)
        return dp
    }

    fun <T : Any, R : BuildingBlock> getAndCacheBlock(type: T, list: MutableList<BuildingBlock>, f: (type: T, list: MutableList<BuildingBlock>) -> R): R {
        val block = f(type, list)
        list.add(block)
        return block
    }
}

