package mg.util.db

import java.sql.ResultSet
import kotlin.reflect.KClass

class ResultSetData {

    companion object {

        fun from(resultSet: ResultSet): MutableList<ResultSetData> {

            val rows = mutableListOf<ResultSetData>()

            return rows
        }
    }


}

data class ResultSetDatas(
        val rows: List<List<ResultSetDataCell>>
)

data class ResultSetDataCell(
        val data: Any,
        val type: String,
        val name: String,
)











