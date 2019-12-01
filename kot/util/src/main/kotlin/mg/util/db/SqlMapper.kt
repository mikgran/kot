package mg.util.db

import mg.util.common.Common
import mg.util.db.dsl.DslMapper
import mg.util.db.dsl.mysql.Sql
import mg.util.functional.Opt2.Factory.of
import kotlin.reflect.KCallable

// mysql dialect object to sql mapper
class SqlMapper {

    fun <T : Any> buildFind(metadata: Metadata<T>): String {
        val sql = Sql() select metadata.type
        return DslMapper.map(sql.list())
    }

    fun <T : Any> buildDrop(metadata: Metadata<T>): String {

        return of(metadata)
                .map { "DROP TABLE IF EXISTS ${it.uid}" }
                .getOrElseThrow { Exception("Unable to build drop table for ${metadata.type::class}") } ?: ""
    }

    fun <T : Any> buildInsert(metadata: Metadata<T>): String {

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

    fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {
        val sql = Sql() create metadata.type
        return DslMapper.map(sql.list())
    }
}