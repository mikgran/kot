package mg.util.db

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

    fun <T: Any> buildMetadata(t: T) : Metadata {

        val ormMetadata = Metadata(0)

        print(t::class.members.size)

        t::class.members.forEach{ m -> print(m) }



        return ormMetadata
    }



}