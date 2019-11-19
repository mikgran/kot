package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import kotlin.reflect.KProperty1

// TODO: handle updates
open class UpdateBlock<T>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {
    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    override fun build(dp: DslParameters): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun buildFields(dp: DslParameters): String = ""
}