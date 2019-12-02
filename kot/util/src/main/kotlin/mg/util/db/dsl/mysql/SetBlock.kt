package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock

open class SetBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {


    open fun <T : Any> newValue(blocks: MutableList<BuildingBlock>, type: T, operation: String) = ValueBlock(blocks, type, operation)

    open infix fun <T : Any> eq(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, "=") }
    open infix fun <T : Any> lt(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, "<") }
    open infix fun <T : Any> gt(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, ">") }


}
