package mg.util.db.dsl

import kotlin.reflect.KProperty1

// TODO: handle updates
data class UpdateBlock<T>(override val blocks: MutableList<BuildingBlock>, val type: T) : BuildingBlock() {

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }
}