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
                .map { metadata -> buildTypeDefinitions(metadata) }
                .map { listOfTypeDefinitions -> listOfTypeDefinitions.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        var createStringPreFix = "CREATE TABLE ${buildTableName(metadata)}(id MEDIUMINT NOT NULL AUTO_INCREMENT, "
        var createStringPostFix = ")"

        return "$createStringPreFix$typeDefinitionsCommaSeparated$createStringPostFix"
    }

    private fun <T: Any> buildTableName(metadata: Metadata<T>): String {

        return Opt2.of(metadata)
                .map { m -> m.name }
                .map { m -> m.toUpperCase() }
                .map { m -> if (m.endsWith("s", true)) m else m + "S"  }
                .getOrElse("")
    }

    fun <T : Any> buildTypeDefinitions(metadata: Metadata<T>): List<String> {

        return Opt2.of(metadata)
                .map { m -> m.type::class.declaredMemberProperties }
                .map { p -> p.map { property -> getMySQLTypeString(property) } }
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