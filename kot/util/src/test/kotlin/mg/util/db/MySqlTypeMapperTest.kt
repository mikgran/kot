package mg.util.db

import mg.util.functional.Opt2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

internal class MySqlTypeMapperTest {

    private val person = DBOTest.Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    @Test
    fun testMapping() {

        val metadata: Metadata<DBOTest.Person> = dbo.buildMetadata(person)

        val candidates = Opt2.of(metadata)
                .map { it.type::class.declaredMemberProperties }
                .map { it.map(MySqlTypeMapper::getTypeString) }
                .getOrElse(emptyList())

        val expectedFieldDefinitions = listOf("firstName VARCHAR(64) NOT NULL", "lastName VARCHAR(64) NOT NULL")

        assertNotNull(candidates)
        assertEquals(2, candidates.size)
        assertTrue(expectedFieldDefinitions.containsAll(candidates))
    }

    // @Test
    fun testMapping2() {

    }


}