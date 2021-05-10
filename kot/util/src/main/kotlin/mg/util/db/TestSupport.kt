package mg.util.db

import mg.util.common.Common
import mg.util.db.config.DBConfig
import mg.util.db.config.TestConfig
import mg.util.functional.Opt2
import mg.util.functional.toOpt
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

        private fun deleteFromUid(statement: Statement, uid: String) = Common.nonThrowingBlock { statement.executeUpdate("DELETE FROM $uid") }
        private fun dropTableUid(statement: Statement, uid: String) = Common.nonThrowingBlock { statement.executeUpdate("DROP TABLE IF EXISTS $uid") }

        fun dropJoinTables(tables: List<Pair<Any, Any>>) {
            val statement = DBConfig(TestConfig())
                    .toOpt()
                    .map(DBConfig::connection)
                    .map(Connection::createStatement)

            tables.map { pair ->
                val firstId = UidBuilder.buildUniqueId(pair.first)
                val secondId = UidBuilder.buildUniqueId(pair.second)
                val dropId = "$firstId$secondId"

                statement.map {
                    deleteFromUid(it, dropId)
                    dropTableUid(it, dropId)
                }
            }
        }

        fun dropJoinTable(a1: Any, a2: Any) {
            val a1Id = UidBuilder.buildUniqueId(a1)
            val a2Id = UidBuilder.buildUniqueId(a2)
            val dropId = a1Id + a2Id

            DBConfig(TestConfig())
                    .toOpt()
                    .map { it.connection }
                    .map(Connection::createStatement)
                    .ifPresent { stmt ->
                        deleteFromUid(stmt, dropId)
                        dropTableUid(stmt, dropId)
                    }
        }
    }
}