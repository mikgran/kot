package mg.util.db.functional.data

@Suppress("unused")
open class DataCell(val data: Any = Any(),
                    val type: String = "",
                    val name: String = "",
                    val isEmpty: Boolean = true,
                    val tableName: String = "")