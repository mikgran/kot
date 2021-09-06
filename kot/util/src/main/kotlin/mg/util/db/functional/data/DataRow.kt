package mg.util.db.functional.data

import mg.util.functional.mapIf
import java.util.*

open class DataRow(
        private val columns: List<ResultSetDataCell> = LinkedList(),
        private val columnNames: List<String> = LinkedList(),
) {
    fun isEmpty() = columns.isEmpty()
    fun size() = columns.size

    operator fun get(column: Int): DataCell =
            (column > -1 && column < columns.size)
                    .mapIf { columns[column] }
                    .mapTo(DataCell::class)
                    .getOrElse { EmptyResultSetDataCell() }

    operator fun get(columnName: String): DataCell = this[columnNames.indexOf(columnName)]
}