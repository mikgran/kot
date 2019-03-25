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

    fun <T: Any> buildOrmMetadata(t: T) : OrmMetadata {

        val ormMetadata = OrmMetadata(0)

        print(t::class.members.size)





        return ormMetadata
    }



}