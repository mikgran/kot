package mg.util.db.functional.data

import mg.util.common.Common.classSimpleName

open class DataCell(val data: Any = Any(),
                    val type: String = "",
                    val name: String = "",
                    val isEmpty: Boolean = true,
                    val tableName: String = "") {

    override fun toString(): String {
        val propertiesFormatted = "%-10s %-10s %-18s %-10s".format("$data,", "$type,", "$name,", tableName)
        return "${classSimpleName()} ( $propertiesFormatted )"
    }
}