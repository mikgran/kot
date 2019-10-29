package mg.util.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SqlMapperFactoryTest {

    @Test
    fun testGet() {

        val sqlMapperCandidate = SqlMapperFactory.get("mysql")

        assertTrue(MySqlMapper::class == sqlMapperCandidate::class)

        val sqlMapperCandidate2 = SqlMapperFactory.get("xxx") // case defaults

        assertTrue(MySqlMapper::class == sqlMapperCandidate2::class)
    }


}