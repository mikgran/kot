package mg.util.db.functional

import java.sql.ResultSet

// Iterates over ResultSet. Meant only for rewindable ResultSets.
class ResultSetIterator private constructor(private val resultSet: ResultSet) : Iterator<ResultSet>, Iterable<ResultSet> {

    @Synchronized
    private fun <T> synchronizedWithResultSet(block: (ResultSet) -> T) = block(resultSet)

    override fun hasNext(): Boolean {
        return synchronizedWithResultSet {
            val hasNextRow = it.next()
            if (hasNextRow) {
                it.previous()
            }
            hasNextRow
        }
    }

    override fun next(): ResultSet {
        return synchronizedWithResultSet {
            it.next()
            it
        }
    }

    override fun iterator(): Iterator<ResultSet> = this

    fun <T : Any> map(mapper: (ResultSet) -> T): List<T> {
        val listT = mutableListOf<T>()
        synchronizedWithResultSet {
            while (it.next()) {
                listT += mapper(it)
            }
        }
        return listT.toList()
    }

    companion object {
        fun iof(rs: ResultSet): ResultSetIterator = of(rs)
        fun of(rs: ResultSet): ResultSetIterator = ResultSetIterator(rs)
    }
}

fun ResultSet.toResultSetIterator(): ResultSetIterator = ResultSetIterator.of(this)

data class PrintData(var headerLengths: MutableList<Int>, var columnCount: Int, var headers: List<String>, var rows: List<List<String>>)

internal fun ResultSet.getPrintData(): PrintData {
    val headerLengths = mutableListOf<Int>()
    val columnCount = metaData.columnCount
    val headers = (1..columnCount).map { index ->
        metaData.getColumnName(index)
                .also { headerLengths.add(it.length) }
    }

    beforeFirst()
    val rows: List<List<String>> = toResultSetIterator().map {
        (1..columnCount).map { index ->
            val column = getString(index)
            if (headerLengths[index - 1] < column.length) {
                headerLengths[index - 1] = column.length
            }
            column
        }
    }

    return PrintData(headerLengths, columnCount, headers, rows)
}

fun PrintData.prettyFormat(): List<List<String>> {
    val allRows = listOf(headers) + rows

    return allRows.map { row ->
        (0 until columnCount).map { index ->
            val column = row[index]
            String.format(column + " ".repeat(headerLengths[index] - column.length))
        }
    }
}

fun ResultSet.print(): ResultSet {
    getPrintData()
            .prettyFormat()
            .joinToString("\n") {
                it.joinToString(" ")
            }
            .also(::print)

    return this
}

