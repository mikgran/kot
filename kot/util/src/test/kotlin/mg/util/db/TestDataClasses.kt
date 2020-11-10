package mg.util.db

class TestDataClasses {
    // Note: prefix classes with their test class camel case capitals
    data class DBPersonB(val firstName: String = "", val lastName: String = "")
    data class DBOPerson(val firstName: String = "", val lastName: String = "")
    data class DBOPerson2(val firstName: String = "", val lastName: String = "")
    data class DBOPerson3(val firstName: String = "", val lastName: String = "")
    data class DBOBilling(val amount: String = "", val dboPerson: DBOPerson = DBOPerson("", ""))
    data class DBOBilling2(val amount: String = "", val dboPerson: DBOPerson3 = DBOPerson3("", ""))
    data class DBOSimple(val ffff: String = "aaaa")
    data class DBOSimpleComp(val gggg: String = "cccc")
    data class DBOComposition(val gggg: String = "bbbb", val hhhh: DBOSimple = DBOSimple("cccc"))
    data class DBOMultipleComposition(val iiii: Int = 0, val hhhh: DBOSimple = DBOSimple("cccc"), val ssss: List<DBOSimpleComp> = listOf(DBOSimpleComp("1111"), DBOSimpleComp("2222")))
    data class DSLPerson(val firstName: String = "", val lastName: String = "")
    data class DSLPersonB(val firstName: String = "", val lastName: String = "")
    data class DSLAddress(var fullAddress: String = "")
    data class DSLAddress2(var fullAddress: String = "")
    data class DSLPlace(var address: DSLAddress = DSLAddress(), var rentInCents: Int = 0)
    data class DSLPlace2(var address: DSLAddress2 = DSLAddress2(), var rentInCents: Int = 0)
    data class DSLPlaceDescriptor(val description: String = "", val placeRefId: Int = 0)
    data class DSLFloor(var number: Int = 0)
    data class DSLBuilding(var fullAddress: String = "", var floors: List<DSLFloor> = listOf(DSLFloor(1)))
    data class MTMPerson(val firstName: String = "", val lastName: String = "")
    data class OBPersonB(val firstName: String = "", val lastName: String = "")
    data class SMTPerson(val firstName: String = "", val lastName: String = "")
    data class RSIPerson(val firstName: String = "", val lastName: String = "")
}