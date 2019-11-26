package mg.util.db

import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object UidBuilder {

    fun <T : Any> buildUniqueId(t: T): String {

        val opt = of(t).map(::propertiesOfT)

        return buildUid(opt, t::class.simpleName ?: "").getOrElse("")
    }

    fun <T : Any> build(t: KClass<out T>): String {

        val opt = of(t).map { it.memberProperties.toCollection(ArrayList()) }

        return buildUid(opt, t.simpleName ?: "").getOrElse("")
    }

    private fun <T : Any> buildUid(opt: Opt2<ArrayList<KProperty1<out T, Any?>>>, name: String): Opt2<String> {
        return opt.filter { it.size > 0 }
                .map { it.filter { p -> p.name != "id" } }
                .map { it.fold("") { n, p -> n + p.name } }
                .mapWith(name) { foldedNames, simpleName -> simpleName + foldedNames.hashCode() }
    }

    private fun <T : Any> propertiesOfT(t: T): ArrayList<KProperty1<out T, Any?>> {
        return t::class.memberProperties.toCollection(ArrayList())
    }
}