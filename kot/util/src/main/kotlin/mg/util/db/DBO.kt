package mg.util.db

import mg.util.functional.Opt2
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty

class DBO {


    // ORM describe
    // Metadata:
    // - name of the object
    // - fields

    // Data:
    // - object name
    // - object fields
    // - object field data

    // Methods:
    // - build a deterministic unique name
    // - build definition of fields
    // - build access methods for each property

    // Considerations:
    // - cache all Metadata objects?

    fun <T : Any> buildMetadata(t: T): Metadata {

        val propertiesOfT = propertiesOfT(t)

        val uid = buildUniqueId(t)

        val name = t::class.simpleName ?: ""

        return Metadata(propertiesOfT.size, name, uid)
    }

    private fun <T : Any> propertiesOfT(t: T): ArrayList<KCallable<*>> {

        val membersOfT = t::class.members

        return membersOfT.filter { member -> member is KProperty<*> }
                .toCollection(ArrayList())
    }

    fun <T : Any> buildUniqueId(t: T): String {

        val uid = Opt2.of(t)
                .map(::propertiesOfT)
                .filter { propertiesOfT -> propertiesOfT.size > 0 }
                .map { propertiesOfT -> propertiesOfT.fold("") { n, p -> n + p.name } }
                .map { foldedNames -> t::class.simpleName + foldedNames.hashCode() }

        return uid.getOrElse("")
    }

}