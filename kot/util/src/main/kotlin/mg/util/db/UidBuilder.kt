package mg.util.db

import mg.util.common.Cache
import mg.util.functional.toOpt
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object UidBuilder {

    private val uidCache: Cache<KClass<*>, String> = Cache.of()

    internal fun uids() = uidCache

    fun <T : Any> buildUniqueId(t: T): String {
        return t.toOpt()
                .map { it::class }
                .map(::build)
                .getOrElse("")
    }

    // TODO 10 add type coverage, and multiple packages support
    fun <T : Any> build(t: KClass<out T>): String {
        return uidCache.getOrCache(t) { buildUid(t) }
    }

    private fun <T : Any> buildUid(t: KClass<out T>) = t.toOpt()
            .map { it.memberProperties.toCollection(ArrayList()) }
            .filter { it.size > 0 }
            .xmap { filter { it.name != "id" } }
            .xmap { fold("") { n, p -> n + p.name } }
            .map { it.hashCode() }
            .map { if (it < 0) (it and 0x7fffffff) else it }
            .mapWith(t.simpleName) { hashCode, simpleName -> simpleName + hashCode }
            .getOrElse { "" }
}