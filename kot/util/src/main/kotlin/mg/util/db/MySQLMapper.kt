package mg.util.db

import mg.util.common.Common
import mg.util.functional.Opt2
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

// mysql dialect object to sql mapper
object MySQLMapper : SqlMapper {

    override fun <T : Any> buildFind(metadata: Metadata<T>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> buildInsert(metadata: Metadata<T>): String {

        val sqlInsert = Opt2.of(metadata)
                .map { m -> buildSqlInsert(m) }
                .getOrElseThrow { Exception("Unable to build insert for ${metadata.type::class}") }

        return sqlInsert ?: ""
    }

    private fun <T : Any> buildSqlInsert(metadata: Metadata<T>): String {

        val padding1 = "INSERT INTO ${metadata.uid} ("
        val padding2 = ") VALUES ("
        val padding3 = ")"

        val properties = Opt2.of(metadata)
                .map { m -> m.properties }
                .getOrElseThrow { Exception("No properties found in metadata") }

        val fieldsCommaSeparated = Opt2.of(properties)
                .map { p -> p.map { p -> p.name }.joinToString(", ") }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to create insert fields string (field1, field2)") }

        val fieldsValuesCommaSeparated = Opt2.of(properties)
                .map { p -> p.map { p -> "'${getFieldValueAsString(p, metadata.type)}'" }.joinToString(", ") }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to fetch field values for insert ('val1', 'val2')") }

        return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"
    }

    private fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()

    override fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {

        val sqlFieldDefinitionsCommaSeparated = Opt2.of(metadata)
                .map { metadata -> buildSqlFieldDefinitions(metadata) }
                .map { listOfFieldDefinitions -> listOfFieldDefinitions.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create for ${metadata.type::class}") }

        var createStringPreFix = "CREATE TABLE ${metadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT, "
        var createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    fun <T : Any> buildSqlFieldDefinitions(metadata: Metadata<T>): List<String> {

        return Opt2.of(metadata)
                .map { m -> m.type::class.declaredMemberProperties }
                .map { mp -> mp.map { property -> getMySQLTypeString(property) } }
                .getOrElse(emptyList())
    }

    private fun getMySQLTypeString(type: KProperty1<out Any, Any?>): String {

        // TOIMPROVE: move to it's own class?
        val kClass = type.returnType.classifier as KClass<*>
        return when (kClass.simpleName) {
            "String" -> "${type.name} VARCHAR(64) NOT NULL"
            else -> ""
        }
    }

    // TOIMPROVE: create direct sql mappers too?
    fun <T : Any> create(t: T): String {

        return t.toString()
    }

    fun <T : Any> find(t: T): String {

        return t.toString()
    }
}