package mg.util.db

import mg.util.common.Common.classSimpleName
import mg.util.db.functional.toResultSetIterator
import mg.util.functional.mapIf
import java.sql.ResultSet
import java.util.*

class ResultSetData private constructor() {

    private val rows: MutableList<ResultSetDataRow> = LinkedList()
    internal fun contents() = rows

    operator fun get(row: Int): DataRow =
            (row > 0 && row <= rows.size)
                    .mapIf { rows[row] }
                    .mapTo(DataRow::class)
                    .getOrElse { EmptyResultSetDataRow() }

    fun isEmpty() = rows.isEmpty()

    override fun toString(): String {

        val rowStrings: MutableList<String> = LinkedList()

        rows.isNotEmpty().mapIf {
            rowStrings += rows.first().rsColumnNames.joinToString(", ")
        }
        rows.forEach {
            rowStrings += it.rsColumns.joinToString(", ") { cell ->
                cell.cellData.toString()
            }
        }
        return this.classSimpleName() + ":\n" + rowStrings.joinToString("\n")
    }

    companion object {

        fun from(resultSet: ResultSet): ResultSetData {

            val data = ResultSetData()
            val columnNames = mutableListOf<String>()
            val columnCount = resultSet.metaData.columnCount

            (1..columnCount).forEach {
                columnNames += resultSet.metaData.getColumnName(it)
            }

            resultSet.toResultSetIterator()
                    .forEach { rs ->

                        val cells = mutableListOf<ResultSetDataCell>()
                        (1..columnCount).forEach { index ->
                            val newCell = ResultSetDataCell(
                                    cellData = rs.getString(index),
                                    cellType = rs.metaData.getColumnTypeName(index),
                                    cellName = rs.metaData.getColumnName(index),
                            )
                            cells.add(newCell)
                        }

                        data.rows.add(ResultSetDataRow(cells, columnNames))
                    }
            return data
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











