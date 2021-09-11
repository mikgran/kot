package mg.util.db.functional.data

import mg.util.common.Common.classSimpleName

data class ResultSetDataCell(
        val cellData: Any,
        val cellType: String,
        val cellName: String,
        val cellTableName: String,
) : DataCell(cellData, cellType, cellName, isEmpty = false, cellTableName) {
    override fun toString(): String {
        val propertiesFormatted = "%-10s %-10s %-18s %-10s".format("$cellData,", "$cellType,", "$cellName,", cellTableName)
        return "${classSimpleName()} ( $propertiesFormatted )"
    }
}