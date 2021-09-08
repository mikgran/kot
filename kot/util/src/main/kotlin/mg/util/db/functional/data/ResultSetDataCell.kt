package mg.util.db.functional.data

data class ResultSetDataCell(
        val cellData: Any,
        val cellType: String,
        val cellName: String,
        val cellTableName: String,
) : DataCell(cellData, cellType, cellName, isEmpty = false, cellTableName)