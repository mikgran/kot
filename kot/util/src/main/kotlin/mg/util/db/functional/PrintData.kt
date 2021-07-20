package mg.util.db.functional

import java.sql.ResultSet

class PrintData(resultSet: ResultSet) {

    private var headerLengths: List<Int>
    private var columnCount: Int
    private var headers: List<String>
    private var rows: List<List<String>>

    init {
        val headerLengthsMut = mutableListOf<Int>()
        columnCount = resultSet.metaData.columnCount
        headers = (1..columnCount).map { index ->
            resultSet.metaData.getColumnName(index)
                    .also { headerLengthsMut.add(it.length) }
        }
        resultSet.beforeFirst()
        val rowsMut = resultSet.toResultSetIterator().map {
            (1..columnCount).map { index ->
                val column = resultSet.getString(index)
                if (headerLengthsMut[index - 1] < column.length) {
                    headerLengthsMut[index - 1] = column.length
                }
                column
            }
        }
        headerLengths = headerLengthsMut
        rows = rowsMut
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

    fun print() = prettyFormat().also(::printListOfColumns)

    private fun printListOfColumns(listList: List<List<String>>) {
        listList.joinToString(", ") {
            it.joinToString(" ")
        }.also(::print)
    }
}

internal fun ResultSet.getPrintData() = PrintData(this)

fun ResultSet.print(): ResultSet = this.also { getPrintData().print() }
