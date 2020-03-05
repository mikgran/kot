package mg.util.db

import mg.util.common.Common
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.functional.Opt2
import java.sql.Connection
import java.sql.Statement

class TestSupport {

    companion object {

        fun dropTables(objects: List<Any>) {

            val dbConfig = DBConfig(TestConfig())

            val tableUids = Opt2.of(objects)
                    .filter { it.isNotEmpty() }
                    .ifMissingThrow { Exception("No cleanup targets!") }
                    .xmap { map { UidBuilder.buildUniqueId(it) } }
                    .get()!!

            forStatement(dbConfig, tableUids) { stmt, uid ->
                deleteFromUid(stmt, uid)
                dropTableUid(stmt, uid)
            }
        }

        private fun forStatement(dbConfig: DBConfig, uids: List<String>, mapper: (Statement, String) -> Unit) {
            Opt2.of(dbConfig.connection)
                    .map(Connection::createStatement)
                    .ifPresent { stmt -> uids.forEach { uid -> mapper(stmt, uid) } }
        }

        private fun deleteFromUid(s: Statement, uid: String) = Common.nonThrowingBlock { s.executeUpdate("DELETE FROM $uid") }
        private fun dropTableUid(s: Statement, uid: String) = Common.nonThrowingBlock { s.executeUpdate("DROP TABLE IF EXISTS $uid") }
    }
}