package mg.util.db.functional

import java.sql.ResultSet

class PrintData(resultSet: ResultSet) {

    private val headerLengths: List<Int>
    private val columnCount: Int
    private val headers: List<String>
    private val rows: List<List<String>>

    init {
        val headerLengthsMut = mutableListOf<Int>()
        columnCount = resultSet.metaData.columnCount
        headers = (1..columnCount).map { index ->
            resultSet.metaData.getColumnName(index)
                    .also { headerLengthsMut.add(it.length) }
        }

        resultSet.beforeFirst()
        rows = resultSet.toResultSetIterator().map {
            (1..columnCount).map { index ->
                val column = resultSet.getString(index)
                if (headerLengthsMut[index - 1] < column.length) {
                    headerLengthsMut[index - 1] = column.length
                }
                column
            }
        }
        headerLengths = headerLengthsMut
    }

    fun prettyFormat(): List<List<String>> {
        val allRows = listOf(headers) + rows

        return allRows.map { row ->
            (0 until columnCount).map { index ->
                val column = row[index]
                String.format(column + " ".repeat(headerLengths[index] - column.length))
            }
        }
    }

    fun print() = printListOfColumns(prettyFormat())

    private fun printListOfColumns(rowsOfColumns: List<List<String>>) {
        rowsOfColumns.joinToString("\n") { column ->
            column.joinToString(" ")
        }.also(::print)
    }
}

fun ResultSet.getPrintData() = PrintData(this)
fun ResultSet.printRows(): ResultSet = this.also { getPrintData().print() }
fun ResultSet.printColumnInfo(): ResultSet {
    val columnString =
            (1..metaData.columnCount).joinToString(" ") { index ->
                "\n${metaData.getTableName(index)}.${metaData.getColumnName(index)}"
            }
    println(columnString)
    return this
}

