package mg.util.db

class Metadata<T>(fieldCount: Int = 0, name: String = "", uid: String = "", type: T) {
    val fieldCount: Int = fieldCount // TOIMPROVE: remove?
    val name: String = name
    val uid: String = uid
    val type : T = type
}
