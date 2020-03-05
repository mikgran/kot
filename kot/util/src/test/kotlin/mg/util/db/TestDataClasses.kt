package mg.util.db


// XXX: 10 remove test dependencies from each test
class TestDataClasses {
    // Note: prefix classes with their test classes name
    data class DBPersonB(val firstName: String = "", val lastName: String = "")
    data class DBOPerson(val firstName: String = "", val lastName: String = "")
    data class DBOPerson2(val firstName: String = "", val lastName: String = "")
    data class DBOBilling(val amount: String = "", val dboPerson: DBOPerson = DBOPerson("", ""))
    data class DBOSimple(val ffff: String = "aaaa")
    data class DBOSimpleComp(val gggg: String = "cccc")
    data class DBOComposition(val gggg: String = "bbbb", val hhhh: DBOSimple = DBOSimple("cccc"))
    data class DBOMultipleComposition(val iiii: Int = 0, val hhhh: DBOSimple = DBOSimple("cccc"), val ssss: List<DBOSimpleComp> = listOf(DBOSimpleComp("1111"), DBOSimpleComp("2222")))
    data class DSLPerson(val firstName: String = "", val lastName: String = "")
    data class DSLPersonB(val firstName: String = "", val lastName: String = "")
    data class DSLAddress(var fullAddress: String = "")
    data class DSLPlace(var address: DSLAddress = DSLAddress(), var rentInCents: Int = 0)
    data class DSLPlaceDescriptor(val description: String = "", val placeRefId: Int = 0)
    data class DSLFloor(var number: Int = 0)
    data class DSLBuilding(var fullAddress: String = "", var floors: List<DSLFloor> = listOf(DSLFloor(1)))
    data class SMTPerson(val firstName: String = "", val lastName: String = "")

    data class RSIPerson(val firstName: String = "", val lastName: String = "")

    data class Person(val firstName: String = "", val lastName: String = "")

    data class PersonB(val firstName: String = "", val lastName: String = "")


    data class MTMPerson(val firstName: String = "", val lastName: String = "")
}