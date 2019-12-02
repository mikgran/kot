package mg.util.db.dsl.mysql

import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2
import mg.util.functional.rcv
import kotlin.reflect.KProperty1

// TODO: handle updates
open class UpdateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    infix fun <T: KProperty1<*, *>> set(type: T): SetBlock<T> = getAndCacheBlock(type, blocks) { t, b -> SetBlock(b, t)}
    // infix fun <T : KProperty1<*, *>> where(type: T): WhereBlock<T> = getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }

    override fun toString(): String = "${simpleName()}(type=$type)"

    override fun buildDelete(dp: DslParameters): String {

        val builder = Opt2.of(StringBuilder())
                .rcv {
                    append("UPDATE ") // "UPDATE $uid SET firstName = 'newFirstName', lastName = 'newLastName' WHERE firstName = 'firstName'"
                    append(dp.uniqueId)
                    append(" ")
                }

        return builder.get().toString()
    }


}