package mg.util.db

class TestDataClasses {
    // Note: prefix classes with their test class camel case capitals
    data class DBPersonB(val firstName: String = "", val lastName: String = "")
    data class DBOPerson(val firstName: String = "", val lastName: String = "")
    data class DBOPerson2(val firstName: String = "", val lastName: String = "")
    data class DBOPerson3(val firstName: String = "", val lastName: String = "")
    data class CPerson1(val firstName: String = "", val lastName: String = "")
    data class CPerson2(val firstName: String = "", val lastName: String = "", val car: CCar = CCar())
    data class CCar(val name: String = "some name")

    // data class CCar2(val name: String = "some name")
    data class DBOBilling(val amount: String = "", val dboPerson: DBOPerson = DBOPerson("", ""))
    data class DBOBilling2(val amount: String = "", val dboPersons: List<DBOPerson3> = listOf(DBOPerson3("a", "a"), DBOPerson3("b", "b")))
    data class DBOSimple(val ffff: String = "aaaa")
    data class DBOSimpleComp(val gggg: String = "cccc")
    data class DBOComposition(val gggg: String = "bbbb", val hhhh: DBOSimple = DBOSimple("cccc"))
    data class DBOMultipleComposition(val iiii: Int = 0, val hhhh: DBOSimple = DBOSimple("cccc"), val ssss: List<DBOSimpleComp> = listOf(DBOSimpleComp("1111"), DBOSimpleComp("2222")))
    data class DSLPerson(val firstName: String = "", val lastName: String = "")
    data class DSLPerson4(val firstName: String = "", val lastName: String = "", var address: DSLAddress4 = DSLAddress4())
    data class DSLPersonB(val firstName: String = "", val lastName: String = "")
    data class DSLAddress(var fullAddress: String = "")
    data class DSLAddress2(var fullAddress: String = "")
    data class DSLAddress3(var fullAddress: String = "")
    data class DSLAddress4(var fullAddress: String = "")
    data class DSLPlace(var address: DSLAddress = DSLAddress(), var rentInCents: Int = 0)
    data class DSLPlace2(var address: DSLAddress2 = DSLAddress2(), var rentInCents: Int = 0)
    data class DSLPlace3(var address: DSLAddress3 = DSLAddress3(), var floors: List<DSLFloor3> = listOf(DSLFloor3(1)), var rentInCents: Int = 0)
    data class DSLPlace4(var handler: DSLPerson4 = DSLPerson4(), var floors: List<DSLFloor4> = listOf(DSLFloor4(1)), var rentInCents: Int = 0)
    data class DSLPlaceDescriptor(val description: String = "", val placeRefId: Int = 0)
    data class DSLFloor(var number: Int = 0)
    data class DSLFloor3(var number: Int = 0)
    data class DSLFloor4(var number: Int = 0)
    data class DSLBuilding(var fullAddress: String = "", var floors: List<DSLFloor> = listOf(DSLFloor(1)))
    data class DSLPersonM1(var name: String = "")
    data class DSLLocationM1(var loc: String = "", var persons: List<DSLPersonM1> = listOf(DSLPersonM1("AAAA")))
    data class DSLAddressM1(var rent: Int = 0, var loc: DSLLocationM1 = DSLLocationM1("BBBB"))
    data class FCAddress(var address: String = "")
    data class FCBill(var amount: Int = 0, var date: String = "")
    data class FCPerson(var fullName: String = "", var address: FCAddress = FCAddress(""), var bills: List<FCBill> = mutableListOf())
    data class MTMPerson(val firstName: String = "", val lastName: String = "")
    data class OBPersonB(val firstName: String = "", val lastName: String = "")
    data class OBSimple(val simple: String = "aaaa")
    data class OBSubComp(val sub: Int = 0)
    data class OBSimpleComp(var comp: String = "cccc", var sub: OBSubComp = OBSubComp(10))
    data class OBMultipleComposition(val compotisionValue: Int = 0, val obSimple: OBSimple = OBSimple(""), val obSimpleComps: List<OBSimpleComp> = listOf(OBSimpleComp(""), OBSimpleComp("")))
    data class SMTPerson(val firstName: String = "", val lastName: String = "")
    data class RSIPerson(val firstName: String = "", val lastName: String = "")
}