package mg.util.db

import mg.util.functional.Opt2
import java.util.ArrayList
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object UidBuilder {

    fun <T : Any> buildUniqueId(t: T): String {

        val uid = Opt2.of(t)
                .map(::propertiesOfT)
                .filter { it.size > 0 }
                .map { it.filter { p -> p.name != "id" } }
                .map { it.fold("") { n, p -> n + p.name } }
                .mapWith(t) { foldedNames, type -> type::class.simpleName + foldedNames.hashCode() }

        return uid.getOrElse("")
    }

    private fun <T : Any> propertiesOfT(t: T): ArrayList<KProperty1<out T, Any?>> =
            t::class.memberProperties.toCollection(ArrayList())
}