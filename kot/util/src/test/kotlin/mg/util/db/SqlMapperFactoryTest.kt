package mg.util.db

import mg.util.db.dsl.SqlMapperFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import mg.util.db.dsl.mysql.Sql as MySql
import mg.util.db.dsl.oracle.Sql as OrSql

internal class SqlMapperFactoryTest {

    @Test
    fun testGet() {

        val sqlMapperCandidate = SqlMapperFactory.get("mysql")

        assertTrue(MySql::class == sqlMapperCandidate.sql::class)

        val sqlMapperCandidate2 = SqlMapperFactory.get("xxx") // case defaults

        assertTrue(MySql::class == sqlMapperCandidate2.sql::class)

        val sqlMapperCandidate3 = SqlMapperFactory.get("oracle")

        assertTrue(OrSql::class == sqlMapperCandidate3.sql::class)
    }


}