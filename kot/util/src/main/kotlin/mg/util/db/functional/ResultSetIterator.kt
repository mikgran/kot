package mg.util.db.functional

import java.sql.ResultSet

class ResultSetIterator private constructor(private val resultSet: ResultSet) : Iterator<ResultSet>, Iterable<ResultSet> {

    override fun hasNext(): Boolean {
        return resultSet.next()
    }

    override fun next(): ResultSet {
         // TOIMPROVE: find out if there is a better way of doing this
        return resultSet
    }

    override fun iterator(): Iterator<ResultSet> {
        return this
    }

    fun <T : Any> map(mapper: (ResultSet) -> T): List<T> {
        val listR = mutableListOf<T>()
        while (resultSet.next()) {
            listR.add(mapper(resultSet))
        }
        return listR.toList()
    }

    companion object {

        fun of(rs: ResultSet): ResultSetIterator {
            return ResultSetIterator(rs)
        }
    }
}