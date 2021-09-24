package mg.util.db

import mg.util.db.functional.data.ResultSetData
import mg.util.functional.mapIf

class JoinSelectObjectBuilder {

    companion object {
        /*
             row: 0
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obmultiplecomposition263976979 )
                 ResultSetDataCell ( 555,       MEDIUMINT, compositionValue,  obmultiplecomposition263976979 )
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obsimple1245196722 )
                 ResultSetDataCell ( 1111,      VARCHAR,   simple,            obsimple1245196722 )
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obsimplecomp950501585 )
                 ResultSetDataCell ( AAAA,      VARCHAR,   comp,              obsimplecomp950501585 )
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obsubcomp114240 )
                 ResultSetDataCell ( 77,        MEDIUMINT, sub,               obsubcomp114240 )

                row: 1
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obmultiplecomposition263976979 )
                 ResultSetDataCell ( 555,       MEDIUMINT, compositionValue,  obmultiplecomposition263976979 )
                 ResultSetDataCell ( 1,         MEDIUMINT, id,                obsimple1245196722 )
                 ResultSetDataCell ( 1111,      VARCHAR,   simple,            obsimple1245196722 )
                 ResultSetDataCell ( 2,         MEDIUMINT, id,                obsimplecomp950501585 )
                 ResultSetDataCell ( BBBB,      VARCHAR,   comp,              obsimplecomp950501585 )
                 ResultSetDataCell ( 2,         MEDIUMINT, id,                obsubcomp114240 )
                 ResultSetDataCell ( 88,        MEDIUMINT, sub,               obsubcomp114240 )

            id compositionValue id simple id comp id sub
            1  555              1  1111   1  AAAA 1  77
            1  555              1  1111   2  BBBB 2  88
        */
        fun <T : Any> build(rsData: ResultSetData, typeT: T): MutableList<T> {

            val mutableListOfT = mutableListOf<T>()

            /*
                - group by uids
                - collect uids
                - uids distinct by id
                - map all new typeT
                - assign to owners by id
             */

            rsData.isNotEmpty()
                    .mapIf {

                        rsData[0].groupBy { it.tableName }
                                .forEach { key, cells ->

                                }
                    }





            return mutableListOfT
        }
    }
}
