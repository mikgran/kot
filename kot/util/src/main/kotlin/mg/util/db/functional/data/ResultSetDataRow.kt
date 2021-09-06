package mg.util.db.functional.data

data class ResultSetDataRow(
        val rsColumns: List<ResultSetDataCell>,
        val rsColumnNames: List<String>
) : DataRow(rsColumns, rsColumnNames)