package mg.util.db

import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object UidBuilder {

    fun <T : Any> buildUniqueId(t: T): String {
        return of(t)
                .map { it::class }
                .map(::build)
                .getOrElse("")
    }

    fun <T : Any> build(t: KClass<out T>): String {
        return of(t).map { it.memberProperties.toCollection(ArrayList()) }
                .filter { it.size > 0 }
                .xmap { filter { it.name != "id" } }
                .xmap { fold("") { n, p -> n + p.name } }
                .map { it.hashCode() }
                .map { if (it < 0) (it and 0x7fffffff) else it }
                .mapWith(t.simpleName) { hashCode, simpleName -> simpleName + hashCode }
                .getOrElse("")
    }
}