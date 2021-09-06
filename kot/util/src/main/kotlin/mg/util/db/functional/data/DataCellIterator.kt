package mg.util.db.functional.data

class DataCellIterator(private val dataRow: DataRow) : Iterator<DataCell> {
    private var columnIndex = 0
    override fun hasNext(): Boolean = columnIndex <= dataRow.size()
    override fun next(): DataCell = dataRow[columnIndex].also { columnIndex += 1 }
}