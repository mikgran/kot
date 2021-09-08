package mg.util.db.functional.data

import mg.util.common.Common.classSimpleName
import mg.util.db.functional.toResultSetIterator
import mg.util.functional.mapIf
import java.sql.ResultSet
import java.util.*

class ResultSetData private constructor(): Iterable<DataRow> {

    private val rows: MutableList<ResultSetDataRow> = LinkedList()
    internal fun contents() = rows

    operator fun get(row: Int): DataRow =
            (row > -1 && row <= rows.size)
                    .mapIf { rows[row] }
                    .mapTo(DataRow::class)
                    .getOrElse { EmptyResultSetDataRow() }

    fun isEmpty() = rows.isEmpty()
    fun size() = rows.size

    override fun iterator(): Iterator<DataRow> = DataRowIterator(this)

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
                                    cellTableName = rs.metaData.getTableName(index)
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












