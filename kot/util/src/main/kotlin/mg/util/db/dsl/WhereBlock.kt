package mg.util.db.dsl

import kotlin.reflect.KProperty1

data class WhereBlock<T : KProperty1<*, *>>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    infix fun <T> eq(type: T): ValueBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> ValueBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }
}