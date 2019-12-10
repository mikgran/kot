package mg.kot.rgen.service

import mg.util.db.DBConfig
import mg.util.db.persist.DB
import mg.util.db.persist.Persistable
import mg.util.db.persist.annotation.Id
import mg.util.db.persist.annotation.Table
import mg.util.db.persist.annotation.VarChar


class RgenService {

    /*
        Generic rest service for .../<classId>/  CRUD operations
     */

    fun service() {

        val dbConfig = DBConfig.productionConfig
        val db = DB(dbConfig.connection)

        val person2 = Person2()
        db.createTable(person2)

        person2.lastName = "last name"
        person2.firstName = "first name"

        // db.save(person2)

        val persons = db.findAllBy(person2)

        println("persons: ${persons.size}")
        println("persons: $persons")

    }
}

@Table(name = "persons2")
class Person2(
        @get:JvmName("getId_") @set:JvmName("setId_") @Id var id: Long = 0L,
        @VarChar var firstName: String = "",
        @VarChar var lastName: String = ""
) : Persistable() {

    @Override
    override fun toString() : String {
        return "(id: '$id', firstName: '$firstName', lastName: '$lastName')"
    }
}
