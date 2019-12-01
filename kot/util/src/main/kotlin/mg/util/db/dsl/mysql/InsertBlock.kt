package mg.util.db.dsl.mysql

import mg.util.common.Common
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2
import kotlin.reflect.KCallable
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.memberProperties

open class InsertBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildInsert(dp: DslParameters): String {

        val padding1 = "INSERT INTO ${dp.uniqueId} ("
        val padding2 = ") VALUES ("
        val padding3 = ")"

        val properties = Opt2.of(dp.typeT)
                .map { it::class.memberProperties.toCollection(ArrayList()) }
                .getOrElseThrow { Exception("No properties found in metadata") }

        val fieldsCommaSeparated = Opt2.of(properties)
                .map { it.joinToString(", ") { p -> p.name } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to create insert fields string: (field1, field2)") }

        val fieldsValuesCommaSeparated = Opt2.of(properties)
                .map { it.joinToString(", ") { p -> "'${getFieldValueAsString(p, dp.typeT!!)}'" } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to fetch field values for insert: ('val1', 'val2')") }

        return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"
    }

    private fun <T : Any> getFieldValueAsString(p: KCallable<*>, type: T): String = p.call(type).toString()
}
