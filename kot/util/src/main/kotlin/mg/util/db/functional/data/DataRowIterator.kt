package mg.util.db.functional.data

class DataRowIterator(private val rsData: ResultSetData) : Iterator<DataRow> {
    private var rowIndex = 0
    override fun hasNext(): Boolean = rowIndex < rsData.size()
    override fun next(): DataRow = rsData[rowIndex].also { rowIndex++ }
}