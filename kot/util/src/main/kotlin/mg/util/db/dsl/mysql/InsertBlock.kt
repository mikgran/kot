package mg.util.db.dsl.mysql

import mg.util.common.Common
import mg.util.db.dsl.BuildingBlock
import mg.util.db.dsl.DslParameters
import mg.util.functional.Opt2

open class InsertBlock<T : Any>(override val blocks: MutableList<BuildingBlock>, open val type: T) : BuildingBlock(type) {

    override fun buildInsert(dp: DslParameters): String {

        val padding1 = "INSERT INTO ${dp.uniqueId} ("
        val padding2 = ") VALUES ("
        val padding3 = ")"

        val properties = Opt2.of(dp)
                .map { it.typeT::class.mem } XXX
                .getOrElseThrow { Exception("No properties found in metadata") }

        val fieldsCommaSeparated = Opt2.of(properties)
                .map { it.joinToString(", ") { p -> p.name } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to create insert fields string (field1, field2)") }

        val fieldsValuesCommaSeparated = Opt2.of(properties)
                .map { it.joinToString(", ") { p -> "'${getFieldValueAsString(p, metadata.type)}'" } }
                .filter(Common::hasContent)
                .getOrElseThrow { Exception("Unable to fetch field values for insert ('val1', 'val2')") }

        return "$padding1$fieldsCommaSeparated$padding2$fieldsValuesCommaSeparated$padding3"

        return ""
    }

}
