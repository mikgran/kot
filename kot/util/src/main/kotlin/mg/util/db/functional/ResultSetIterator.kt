package mg.util.db.functional

import java.sql.ResultSet

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

    override fun iterator(): Iterator<ResultSet> {
        return this
    }

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

        fun iof(rs: ResultSet): ResultSetIterator {
            return of(rs)
        }

        fun of(rs: ResultSet): ResultSetIterator {
            return ResultSetIterator(rs)
        }
    }
}