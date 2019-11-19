package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.KProperty1

open class WhereBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {
    open infix fun <T : Any> eq(type: T): ValueBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> ValueBlock(b, t) }
    }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun buildSelect(dp: DslParameters): String {

        val operations = of(type)
                .filter { it is KProperty1<*, *> }
                .map { it as KProperty1<*, *> }
                .map { "${dp.uniqueIdAlias}.${it.name} =" }
                .getOrElse("")

        val builder = of(StringBuilder())
                .rcv {
                    append(" WHERE ")
                    append(operations)
                }

        return builder.get().toString()
    }

    override fun buildFields(dp: DslParameters): String = ""
}