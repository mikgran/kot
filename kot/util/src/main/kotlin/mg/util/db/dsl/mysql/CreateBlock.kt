package mg.util.db.dsl.mysql

import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.db.AliasBuilder
import mg.util.db.UidBuilder
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2.Factory.of
import java.lang.reflect.Field
import kotlin.reflect.full.declaredMemberProperties

open class CreateBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildCreate(dp: DslParameters): String {

        // type (f1, f2, f3, custom1, list<custom2>)
        // create table type f1, f2, f4
        // create table custom1 f5
        // alter table custom1 add column typeRef1
        // create table listCustom2 f6
        // alter table listCustom2 add column typeRef2

        val customObjects = getFieldsWithCustoms(dp).map { println(fieldGet(it, dp.typeT!!));it }

        val lists = getFieldsWithListOfCustoms(dp)// .map { println(fieldGet(it, dp.typeT!!)); it }

        of(lists)
                .xmap { map { buildSqlCreateForChild(dp, it) } }

        return buildSqlCreate(dp)
    }

    private fun buildSqlCreateForChild(dp: DslParameters, listField: Field): String {

        of(fieldGet(listField, dp.typeT) as List<*>)
                .filter { it.isNotEmpty() }
                .xmap { filterNotNull().distinctBy { it::class.java.packageName + it::class.java.simpleName } }
                .filter { it.size == 1 }
                .ifMissingThrow { Exception("Multiple type lists not supported") }
                .xmap { first() }
                .map {
                    with (DslParameters()){
                        typeT = it
                        uniqueId = UidBuilder.buildUniqueId(it)
                        uniqueIdAlias = AliasBuilder.build(uniqueId!!)
                    }
                }


        // ALTER TABLE floors ADD COLUMN buildingId INT NOT NULL;
        "ALTER TABLE ${dp.uniqueId} "


        return ""
    }

    private fun fieldGet(it: Field, type: Any?): Any {
        it.isAccessible = true
        return it.get(type)
    }

    private fun isList(field: Field) = List::class.java.isAssignableFrom(field.type)
    private fun isKotlinType(field: Field) = field.type.packageName.contains("kotlin.")
    private fun isJavaType(field: Field) = field.type.packageName.contains("java.")
    private fun typeOfParent(parentDslParameters: DslParameters): Class<out Any> = parentDslParameters.typeT!!::class.java

    private fun getFieldsWithListOfCustoms(parentDslParameters: DslParameters): List<Field> {
        return typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filter(::isList)
                .filter { isSingleTypeList(it, parentDslParameters) }
    }

    private fun isSingleTypeList(field: Field, dp: DslParameters): Boolean {
        field.isAccessible = true
        return (field.get(dp.typeT) as List<*>)
                .filterNotNull()
                .distinctBy { i -> "${i::class.java.packageName}.${i::class.java.simpleName}" }
                .size == 1
    }

    private fun getFieldsWithCustoms(parentDslParameters: DslParameters): List<Field> {
        return typeOfParent(parentDslParameters)
                .declaredFields
                .filterNotNull()
                .filter(::isCustom)
    }

    private fun isCustom(field: Field) = (!(::isList or ::isKotlinType or ::isJavaType))(field)

    private fun buildSqlCreate(dp: DslParameters): String {
        val sqlFieldDefinitionsCommaSeparated = of(dp.typeT)
                .map(::buildSqlFieldDefinitions)
                .map { it.joinToString(", ") }
                .getOrElseThrow { Exception("Unable to build create") }

        val createStringPreFix = "CREATE TABLE IF NOT EXISTS ${dp.uniqueId}(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
        val createStringPostFix = ")"

        return "$createStringPreFix$sqlFieldDefinitionsCommaSeparated$createStringPostFix"
    }

    private fun buildSqlFieldDefinitions(type: Any): List<String> {
        val mapper = MySqlTypeMapper()
        return of(type)
                .map { it::class.declaredMemberProperties }
                .xmap { map(mapper::getTypeString).filter(String::isNotEmpty) }
                .getOrElse(emptyList())
    }
}