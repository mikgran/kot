package mg.util.db.dsl.mysql

// import java.lang.StringBuilder
import mg.util.db.AliasBuilder
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import mg.util.functional.rcv
import kotlin.reflect.full.memberProperties

open class InnerJoinBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock() {

    private var uid: String? = null
    private var uidAlias: String? = null

    infix fun <T : Any> join(type: T): InnerJoinBlock<T> = getAndCacheBlock(type, blocks) { t, b -> InnerJoinBlock(b, t) }

    override fun toString(): String {
        return "${simpleName()}(type=$type)"
    }

    override fun buildSelect(dp: DslParameters): String {

        return of(StringBuilder())
                .rcv {
                    append(" JOIN ") // "JOIN Address a"
                    append(uid)
                    append(" ")
                    append(uidAlias)
                }
                .get()
                .toString()
    }

    override fun buildFields(dp: DslParameters): String {

        of(type).mapWith(dbo) { t, d -> d.buildUniqueId(t) }
                .ifPresent { uid = it }
                .map { AliasBuilder.alias(uid!!) }
                .ifPresent { uidAlias = it }

        return of(type)
                .map { it::class.memberProperties }
                .map { it.joinToString(", ") { p -> "$uidAlias.${p.name}" } }
                .getOrElseThrow { Exception("buildFields: Unable to build join fields") }!!
    }
}

