package mg.util.db.functional.data

import mg.util.functional.mapIf
import java.util.*

open class DataRow(
        val columns: List<DataCell> = LinkedList(),
        val columnNames: List<String> = LinkedList(),
) : Iterable<DataCell> {
    fun isEmpty() = columns.isEmpty()
    fun size() = columns.size

    operator fun get(column: Int): DataCell =
            (column > -1 && column < columns.size)
                    .mapIf { columns[column] }
                    .mapTo(DataCell::class)
                    .getOrElse { DataCell() }

    operator fun get(columnName: String): DataCell = this[columnNames.indexOf(columnName)]
    override fun iterator(): Iterator<DataCell> = DataCellIterator(this)
}