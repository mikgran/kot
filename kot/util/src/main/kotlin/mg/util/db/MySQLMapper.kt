package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KCallable
import kotlin.reflect.full.declaredMemberProperties

// mysql dialect object to sql mapper
object MySqlMapper : SqlMapper {

    override fun <T : Any> buildFind(metadata: Metadata<T>): String {

        val sqlFind = of(metadata)
                .map(::buildSqlFind)
                .get()

        return sqlFind ?: ""
    }

    override fun <T : Any> buildDrop(metadata: Metadata<T>): String {

        return of(metadata)
                .map { "DROP TABLE IF EXISTS ${it.uid}" }
                .getOrElseThrow { Exception("Unable to build drop table for ${metadata.type::class}") } ?: ""
    }

    override fun <T : Any> buildInsert(metadata: Metadata<T>): String {

        return of(metadata)
                .map(::buildSqlInsert)
                .getOrElseThrow { Exception("Unable to build insert for ${metadata.type::class}") } ?: ""
    }

    private fun <T : Any> buildSqlInsert(metadata: Metadata<T>): String {

        val padding1 = "INSERT INTO ${metadata.uid} ("
        val padding2 = ") VALUES ("
        val padding3 = ")"

        val properties = of(metadata)
                .map { it.properties }
                .getOrElseThrow { Exception("No properties found in metadata") }

        val fieldsCommaSeparated = of(properties)
                .map { it.joinToString(", ") { p -> p.name } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to create insert fields string (field1, field2)") }

        val fieldsValuesCommaSeparated = of(properties)
                .map { it.joinToString(", ") { p -> "'${getFieldValueAsString(p, metadata.type)}'" } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to fetch field values for insert ('val1', 'val2')") }

        return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"
    }

    private fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()

    override fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {

        val sqlFieldDefinitionsCommaSeparated = of(metadata)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create for ${metadata.type::class}") }

        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${metadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
        val createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    fun <T : Any> buildSqlFieldDefinitions(metadata: Metadata<T>): List<String> {

        return of(metadata)
                .map { it.type::class.declaredMemberProperties }
                .map { it.map(MySqlTypeMapper::getTypeString) }
                .getOrElse(emptyList())
    }

    // TOIMPROVE: create direct sql mappers too?
    fun <T : Any> create(t: T): String {

        return t.toString()
    }

    private fun <T : Any> buildSqlFind(metadata: Metadata<T>): String = "SELECT * FROM " + metadata.uid
}