package mg.util.db.functional.data

data class ResultSetDataCell(
        val cellData: Any,
        val cellType: String,
        val cellName: String,
) : DataCell(cellData, cellType, cellName, isEmpty = false)