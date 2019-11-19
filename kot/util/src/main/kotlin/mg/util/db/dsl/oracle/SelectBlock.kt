package mg.util.db.dsl.oracle

import mg.util.db.dsl.DslParameters
import mg.util.db.dsl.BuildingBlock
import mg.util.functional.Opt2
import mg.util.functional.rcv
import mg.util.db.dsl.mysql.SelectBlock as MySqlSelectBlock

class SelectBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, override val type: T) : MySqlSelectBlock<T>(blocks, type) {

    override infix fun <T : Any> where(type: T): WhereBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> WhereBlock(b, t) }
    }

    override infix fun <T : Any> join(type: T): InnerJoinBlock<T> {
        return getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }
    }

    override fun buildSelect(dp: DslParameters): String {

        val builder = Opt2.of(StringBuilder())
                .rcv {
                    append("SELECT ") // SELECT p.firstName, p.lastName FROM Persons AS p
                    append(dp.fields)
                    append(" FROM ")
                    append(dp.uniqueId)
                    append(" AS ")
                    append(dp.uniqueIdAlias)
                }

        return builder.get().toString()
    }
}