package mg.util.db.functional.data

import mg.util.common.isIndexValid
import mg.util.functional.mapIf
import java.util.*

open class DataRow(
        val columns: List<DataCell> = LinkedList(),
        val columnNames: List<String> = LinkedList(),
) : Iterable<DataCell> {
    fun isEmpty() = columns.isEmpty()
    fun isNotEmpty() = columns.isNotEmpty()
    fun size() = columns.size

    operator fun get(column: Int): DataCell =
            columns.isIndexValid(column)
                    .mapIf { columns[column] }
                    .mapTo(DataCell::class)
                    .getOrElse { DataCell() }

    operator fun get(columnName: String): DataCell = this[columnNames.indexOf(columnName)]
    override fun iterator(): Iterator<DataCell> = DataCellIterator(this)
}