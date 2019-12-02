package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.KProperty1

open class WhereBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    open fun getSqlKeyWord() = " WHERE "
    open fun <T : Any> newValue(blocks: MutableList<BuildingBlock>, type: T, operation: String) = ValueBlock(blocks, type, operation)

    open infix fun <T : Any> eq(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, "=") }
    open infix fun <T : Any> lt(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, "<") }
    open infix fun <T : Any> gt(type: T): ValueBlock<T> = getAndCacheBlock(type, blocks) { t, b -> newValue(b, t, ">") }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun buildSelect(dp: DslParameters): String {

        val operations = of(type)
                .filter { it is KProperty1<*, *> }
                .map { it as KProperty1<*, *> }
                .map { "${dp.uniqueIdAlias}.${it.name}" }
                .getOrElse("")


        val builder = of(StringBuilder())
                .rcv {
                    append(getSqlKeyWord())
                    append(operations)
                }

        return builder.get().toString()
    }

    override fun buildFields(dp: DslParameters): String = ""

    override fun buildDelete(dp: DslParameters): String {
        return "${getSqlKeyWord()}${(type as KProperty1<*, *>).name}"
    }
}