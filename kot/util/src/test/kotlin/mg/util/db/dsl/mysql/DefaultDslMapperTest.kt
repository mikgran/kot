package mg.util.db.dsl.mysql

import mg.util.db.DBO
import mg.util.db.Metadata
import mg.util.db.TestDataClasses.MTMPerson
import mg.util.db.UidBuilder.buildUniqueId
import mg.util.db.dsl.MySqlTypeMapper
import mg.util.db.dsl.DefaultDslMapper
import mg.util.functional.Opt2.Factory.of
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

internal class DefaultDslMapperTest {

    private val person = MTMPerson("testname1", "testname2")
    private val dbo = DBO(DefaultDslMapper("mysql"))

    data class YClass(val a: Int = 0)
    data class QWithYRef(val q: String = "", val y: YClass = YClass())

    @Test
    fun testMappingClassWithTwoStringFields() {

        val metadata: Metadata<MTMPerson> = dbo.buildMetadata(person)

        val candidates = buildCandidates(metadata)

        val expectedFieldDefinitions = listOf("firstName VARCHAR(64) NOT NULL", "lastName VARCHAR(64) NOT NULL")

        assertContainsExpectedCandidates(candidates, expectedFieldDefinitions)
    }

    // @Test // XXX: 10 fix this
    fun testMappingWithOneToOneRelation() {

        val metadata = dbo.buildMetadata(QWithYRef())
        val yUid = buildUniqueId(YClass())

        val candidates = buildCandidates(metadata)

        val expectedFieldDefinitions = listOf("q VARCHAR(64) NOT NULL", "${yUid}refId MEDIUMINT NOT NULL")

        assertContainsExpectedCandidates(candidates, expectedFieldDefinitions)
    }

    private fun assertContainsExpectedCandidates(candidates: List<String>, expectedFieldDefinitions: List<String>) {
        assertNotNull(candidates)
        assertEquals(2, candidates.size)
        assertTrue(expectedFieldDefinitions.containsAll(candidates), "expected: $expectedFieldDefinitions\ncandidates: $candidates")
    }

    private fun <T : Any> buildCandidates(metadata: Metadata<T>): List<String> {
        return of(metadata)
                .map { it.type::class.declaredMemberProperties }
                .map { it.map { i -> MySqlTypeMapper().getTypeString(i) } }
                .getOrElse(emptyList())
    }


}