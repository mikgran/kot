package mg.util.db

import mg.util.db.functional.data.DataCell
import mg.util.db.functional.data.ResultSetData

class JoinSelectObjectBuilder {

    companion object {
        /*
            DataRow: [0]
             DataCell ( 1,         MEDIUMINT, id,                obmultiplecomposition263976979 )
             DataCell ( 555,       MEDIUMINT, compositionValue,  obmultiplecomposition263976979 )
             DataCell ( 1,         MEDIUMINT, id,                obsimple1245196722 )
             DataCell ( 1111,      VARCHAR,   simple,            obsimple1245196722 )
             DataCell ( 1,         MEDIUMINT, id,                obsimplecomp950501585 )
             DataCell ( AAAA,      VARCHAR,   comp,              obsimplecomp950501585 )
             DataCell ( 1,         MEDIUMINT, id,                obsubcomp114240 )
             DataCell ( 77,        MEDIUMINT, sub,               obsubcomp114240 )

            DataRow: [1]
             DataCell ( 1,         MEDIUMINT, id,                obmultiplecomposition263976979 )
             DataCell ( 555,       MEDIUMINT, compositionValue,  obmultiplecomposition263976979 )
             DataCell ( 1,         MEDIUMINT, id,                obsimple1245196722 )
             DataCell ( 1111,      VARCHAR,   simple,            obsimple1245196722 )
             DataCell ( 2,         MEDIUMINT, id,                obsimplecomp950501585 )
             DataCell ( BBBB,      VARCHAR,   comp,              obsimplecomp950501585 )
             DataCell ( 2,         MEDIUMINT, id,                obsubcomp114240 )
             DataCell ( 88,        MEDIUMINT, sub,               obsubcomp114240 )

            id compositionValue id simple id comp id sub
            1  555              1  1111   1  AAAA 1  77
            1  555              1  1111   2  BBBB 2  88
        */
        fun <T : Any> build(resultSetData: ResultSetData, typeT: T): MutableList<T> {

            val mutableListOfT = mutableListOf<T>()
            val fieldsTypeT = FieldCache.fieldsFor(typeT)

            val results: List<T> = listOf()
            val uniques: MutableMap<String, DataCell> = mutableMapOf()

            resultSetData.forEach { row ->

                // id anotherValue compositionValue id simple id comp id sub
                // 1  666          555              1  1111   1  AAAA 1  77

                println()
            }








            return mutableListOfT
        }
    }

    private fun fillType() {

    }


    private fun <T> newType(typeT: T): T {



        return typeT
    }

}
