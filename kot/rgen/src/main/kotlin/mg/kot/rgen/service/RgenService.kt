package mg.kot.rgen.service

import mg.util.TestConfig
import mg.util.db.DBConfig
import mg.util.db.persist.DB

import mg.util.db.persist.Persistable
import mg.util.db.persist.annotation.Id
import mg.util.db.persist.annotation.Table
import mg.util.db.persist.annotation.VarChar
import java.sql.Connection


class RgenService {

    /*
        Generic rest service for .../<classId>/  CRUD operations
     */

    fun service() {

        val dbConfig = DBConfig(TestConfig())

        val db = DB(dbConfig.connection)

        val person2 = Person2()

        val persons = db.findAllBy(person2)

        println("persons: ${persons.size}")
    }
}

@Table(name = "persons2")
class Person2(
        @get:JvmName("getId_") @set:JvmName("setId_") @Id var id: Long = 0L,
        @VarChar var firstName: String = "",
        @VarChar var lastName: String = ""
) : Persistable() {

    var connection : Connection? = null

    @Override
    override fun toString() : String {
        return "(id: '$id', firstName: '$firstName', lastName: '$lastName')"
    }
}
