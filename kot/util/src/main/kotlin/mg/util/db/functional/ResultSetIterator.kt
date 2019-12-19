package mg.util.db.functional

import java.sql.ResultSet

class ResultSetIterator private constructor(private val resultSet: ResultSet) : Iterator<ResultSet>, Iterable<ResultSet> {

    @Synchronized
    private fun <T> withResultSet(block: (ResultSet) -> T) = block(resultSet)

    override fun hasNext(): Boolean {
        return withResultSet {
            val boolean = it.next()
            it.previous()
            boolean
        }
    }

    override fun next(): ResultSet {
        return withResultSet {
            it.next()
            it
        }
    }

    override fun iterator(): Iterator<ResultSet> {
        return this
    }

    fun <T : Any> map(mapper: (ResultSet) -> T): List<T> {
        val listT = mutableListOf<T>()
        withResultSet { rs ->
            while (rs.next()) {
                listT += mapper(rs)
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