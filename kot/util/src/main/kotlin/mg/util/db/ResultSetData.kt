package mg.util.db

import mg.util.functional.mapIf
import java.sql.ResultSet
import java.util.*

class ResultSetData private constructor() {

    private val rows: List<ResultSetDataRow> = LinkedList()

    operator fun get(row: Int): DataRow =
            (row > -1 && row < rows.size)
                    .mapIf { rows[row] }
                    .mapTo(DataRow::class)
                    .getOrElse { EmptyResultSetDataRow() }

    fun isEmpty() = rows.isEmpty()

    companion object {

        fun from(resultSet: ResultSet): ResultSetData {

            return ResultSetData()
        }

        internal fun empty() = ResultSetData()
    }
}

open class DataRow(
        private val columns: List<ResultSetDataCell> = LinkedList(),
        private val columnNames: List<String> = LinkedList(),
) {
    fun isEmpty() = columns.isEmpty()

    operator fun get(column: Int): DataCell =
            (column > -1 && column < columns.size)
                    .mapIf { columns[column] }
                    .mapTo(DataCell::class)
                    .getOrElse { EmptyResultSetDataCell() }

    operator fun get(columnName: String): DataCell = this[columnNames.indexOf(columnName)]
}

data class ResultSetDataRow(
        val rsColumns: List<ResultSetDataCell>,
        val rsColumnNames: List<String>
) : DataRow(rsColumns, rsColumnNames)

data class EmptyResultSetDataRow(var emptyState: String = "") : DataRow()

open class DataCell(val data: Any = Any(),
                    val type: String = "",
                    val name: String = "",
                    val isEmpty: Boolean = true)

data class ResultSetDataCell(
        val cellData: Any,
        val cellType: String,
        val cellName: String,
) : DataCell(cellData, cellType, cellName, isEmpty = false)

data class EmptyResultSetDataCell(var emptyState: String = "") : DataCell()











