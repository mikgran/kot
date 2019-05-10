package mg.util.db

import kotlin.reflect.KCallable

class Metadata<T>(
        fieldCount: Int = 0,
        name: String = "",
        uid: String = "",
        type: T,
        properties: ArrayList<KCallable<*>>
) {
    val fieldCount: Int = fieldCount // TOIMPROVE: remove?
    val name: String = name
    val uid: String = uid
    val type : T = type
    val properties = properties
}
