package mg.util.db

import mg.util.functional.Opt2
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

// mysql dialect object to sql mapper
object MySQLMapper : SqlMapper {

    override fun <T : Any> buildFind(metadata: Metadata<T>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> buildInsert(metadata: Metadata<T>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {

        val typeDefinitionsCommaSeparated = Opt2.of(metadata)
                .map { metadata -> buildSqlFieldDefinitions(metadata) }
                .map { listOfFieldDefinitions -> listOfFieldDefinitions.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create for ${metadata.type::class}") }

        var createStringPreFix = "CREATE TABLE ${metadata.uid}(id MEDIUMINT NOT NULL AUTO_INCREMENT, "
        var createStringPostFix = ")"

        return "$createStringPreFix$typeDefinitionsCommaSeparated$createStringPostFix"
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