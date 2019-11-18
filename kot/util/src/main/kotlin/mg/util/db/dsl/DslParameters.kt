package mg.util.db.dsl

data class DslParameters(var typeT: Any? = null,
                         var uniqueId: String? = null,
                         var uniqueIdAlias: String? = null,
                         var fields: String? = null,
                         var operations: String? = null,
                         var joins: String? = null
)