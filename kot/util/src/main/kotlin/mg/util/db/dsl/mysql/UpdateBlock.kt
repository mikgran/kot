package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import kotlin.reflect.KProperty1

// TODO: handle updates
open class UpdateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> = getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }

    override fun toString(): String = "${simpleName()}(type=$type)"
}