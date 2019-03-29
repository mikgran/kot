package mg.util.db

import kotlin.reflect.KProperty

class Mapper {


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

    fun <T : Any> buildMetadata(t: T): Metadata {

        val metadata = Metadata(0)

        // print(t::class.members.size)

        val membersOfT = t::class.members

        val listOfMembers = membersOfT.filter { member -> member is KProperty<*> }
                .toCollection(ArrayList())

        listOfMembers.forEach { m -> println(m) }

        return metadata
    }


}