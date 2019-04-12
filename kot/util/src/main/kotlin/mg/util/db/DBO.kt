package mg.util.db

import mg.util.functional.Opt2
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty

// a simple Object-Relational-Mapping class
class DBO {

    // ORM describe
    // Metadata:
    // - name of the object
    // - fields
    // - uid

    // Data:
    // - object name
    // - object fields
    // - object field data

    // Methods:
    // - build a deterministic unique name
    // - build definition of fields
    // - build access methods for each property

    // Requirements:
    // - don't use java: mg.util.db for any of the functions
    // - rewrite using kotlin reflection classes

    // Considerations:
    // - cache all Metadata objects?

    fun <T : Any> buildMetadata(type: T): Metadata<T> {

        val propertiesOfT = propertiesOfT(type)

        val uid = buildUniqueId(type)

        val name = type::class.simpleName ?: ""

        return Metadata(propertiesOfT.size, name, uid, type)
    }

    private fun <T : Any> propertiesOfT(t: T): ArrayList<KCallable<*>> {

        return t::class.members
                .filter { member -> member is KProperty<*> }
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

    fun <T : Any> create(t : T) {

    }

}