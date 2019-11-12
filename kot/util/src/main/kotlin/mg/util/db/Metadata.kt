package mg.util.db

import kotlin.reflect.KCallable

class Metadata<T>(val fieldCount: Int = 0,
                  val name: String = "",
                  val uid: String = "",
                  val type: T,
                  val properties: ArrayList<KCallable<*>>
)
