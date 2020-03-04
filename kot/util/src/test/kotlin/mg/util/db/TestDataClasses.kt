package mg.util.db

// NOTE: remember to prefix things with test classes
// XXX: 10 remove test dependencies from each test
class TestDataClasses {

    data class DBOPerson(val firstName: String = "", val lastName: String = "")
    data class DBOPerson2(val firstName: String = "", val lastName: String = "")
    data class DBOBilling(val amount: String = "", val dboPerson: DBOPerson = DBOPerson("", ""))
    data class DBOSimple(val ffff: String = "aaaa")
    data class DBOSimpleComp(val gggg: String = "cccc")
    data class DBOComposition(val gggg: String = "bbbb", val hhhh: DBOSimple = DBOSimple("cccc"))
    data class DBOMultipleComposition(val iiii: Int = 0, val hhhh: DBOSimple = DBOSimple("cccc"), val ssss: List<DBOSimpleComp> = listOf(DBOSimpleComp("1111"), DBOSimpleComp("2222")))
    data class RSIPerson(val firstName: String = "", val lastName: String = "")

    data class Person(val firstName: String = "", val lastName: String = "")
    data class Billing(val amount: String = "", val person: Person = Person("", ""))
    data class Address(var fullAddress: String = "")
    data class Place(var address: Address = Address(), var rentInCents: Int = 0)
    data class PlaceDescriptor(val description: String = "", val placeRefId: Int = 0)
    data class Floor(var number: Int = 0)
    data class Building(var fullAddress: String = "", var floors: List<Floor> = listOf(Floor(1)))
    data class PersonB(val firstName: String = "", val lastName: String = "")
}