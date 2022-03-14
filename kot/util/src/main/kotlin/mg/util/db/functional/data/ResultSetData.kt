package mg.util.db.functional.data

import mg.util.common.Common.classSimpleName
import mg.util.common.indexValid
import mg.util.db.functional.toResultSetIterator
import mg.util.functional.mapIf
import java.sql.ResultSet
import java.util.*

class ResultSetData private constructor() : Iterable<DataRow> {

    private val rows = LinkedList<DataRow>()
    internal fun contents() = rows

    operator fun get(row: Int): DataRow =
                    rows.indexValid(row)
                    .mapIf { rows[row] }
                    .mapTo(DataRow::class)
                    .getOrElse { DataRow() }

    fun isEmpty() = rows.isEmpty()
    fun isNotEmpty() = rows.isNotEmpty()
    fun size() = rows.size

    override fun iterator() = DataRowIterator(this)

    override fun toString(): String {

        val rowStrings = LinkedList<String>()

        rows.isNotEmpty().mapIf {
            rowStrings += rows.first().columnNames.joinToString(", ")
        }
        rows.forEach {
            rowStrings += it.columns.joinToString(", ") { cell ->
                cell.data.toString()
            }
        }
        return this.classSimpleName() + ":\n" + rowStrings.joinToString("\n")
    }

    companion object {

        fun from(resultSet: ResultSet): ResultSetData {

            val data = ResultSetData()
            val columnNames = LinkedList<String>()
            val columnCount = resultSet.metaData.columnCount

            (1..columnCount).forEach {
                columnNames += resultSet.metaData.getColumnName(it)
            }

            resultSet.toResultSetIterator()
                    .forEach { rs ->

                        val cells = LinkedList<DataCell>()
                        (1..columnCount).forEach { index ->
                            val newCell = DataCell(
                                    data = rs.getString(index),
                                    type = rs.metaData.getColumnTypeName(index),
                                    name = rs.metaData.getColumnName(index),
                                    tableName = rs.metaData.getTableName(index)
                            )
                            cells.add(newCell)
                        }

                        data.rows.add(DataRow(cells, columnNames))
                    }
            return data
        }

        internal fun empty() = ResultSetData()
    }
}












