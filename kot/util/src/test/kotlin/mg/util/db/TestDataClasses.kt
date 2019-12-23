package mg.util.db

class TestDataClasses {
    data class Simple(val ffff: String = "aaaa")
    data class SimpleComp(val gggg: String = "cccc")
    data class Composition(val gggg: String = "bbbb", val hhhh: Simple = Simple("cccc"))
    data class MultipleComposition(val iiii: Int = 0, val hhhh: Simple = Simple("cccc"), val ssss: List<SimpleComp> = listOf(SimpleComp("1111"), SimpleComp("2222")))
    data class Person(val firstName: String = "", val lastName: String = "")
    data class Uuuu(val firstName: String = "", val lastName: String = "")
    data class Billing(val amount: String = "", val person: Person = Person("", ""))
    data class Address(var fullAddress: String = "")
    data class Place(var address: Address = Address(), var rentInCents: Int = 0)
    data class Floor(var number: Int = 0)
    data class Building(var fullAddress: String = "", var floors: List<Floor> = listOf(Floor(1)))
}