package mg.util.db.dsl.mysql

import mg.util.db.DBO
import mg.util.db.TestDataClasses.Person
import mg.util.db.Metadata
import mg.util.db.dsl.SqlMapperFactory
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

internal class MySqlTypeMapperTest {

    private val person = Person("testname1", "testname2")
    private val dbo = DBO(SqlMapperFactory.get("mysql"))

    data class Yyyyy(val a: Int = 0)
    data class Qqqqq(val b: String = "", val c: Yyyyy = Yyyyy())

    @Test
    fun testMappingClassWithTwoStringFields() {

        val metadata: Metadata<Person> = dbo.buildMetadata(person)

        val candidates = buildCandidates(metadata)

        val expectedFieldDefinitions = listOf("firstName VARCHAR(64) NOT NULL", "lastName VARCHAR(64) NOT NULL")

        assertContainsExpectedCandidates(candidates, expectedFieldDefinitions)
    }

    // @Test
    fun testMappingWithOneToOneRelation() {

        val metadata = dbo.buildMetadata(Qqqqq())
        val yyyyyUid = buildUniqueId(Yyyyy())

        val candidates = buildCandidates(metadata)

        val expectedFieldDefinitions = listOf("a VARCHAR(64) NOT NULL", "${yyyyyUid}id MEDIUMINT NOT NULL")

        assertContainsExpectedCandidates(candidates, expectedFieldDefinitions)
    }

    private fun assertContainsExpectedCandidates(candidates: List<String>, expectedFieldDefinitions: List<String>) {
        assertNotNull(candidates)
        assertEquals(2, candidates.size)
        assertTrue(expectedFieldDefinitions.containsAll(candidates))
    }

    private fun <T : Any> buildCandidates(metadata: Metadata<T>): List<String> {
        return of(metadata)
                .map { it.type::class.declaredMemberProperties }
                .map { it.map { i -> MySqlTypeMapper().getTypeString(i) } }
                .getOrElse(emptyList())
    }


}