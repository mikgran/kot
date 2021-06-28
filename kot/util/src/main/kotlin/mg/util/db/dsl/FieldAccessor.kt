package mg.util.db.dsl

import mg.util.common.flatten
import mg.util.db.FieldCache
import mg.util.db.UidBuilder
import java.lang.reflect.Field

class FieldAccessor private constructor() {

    companion object {

        fun fieldGet(field: Field, type: Any?): Any {
            field.isAccessible = true
            return field.get(type)
        }

        fun fieldSet(field: Field, type: Any?, value: Any?) {
            field.isAccessible = true
            field.set(type, value)
        }

        fun uniquesByParent(t: Any, uniquesByParent: HashMap<Any, List<Any>> = HashMap()): HashMap<Any, List<Any>> {
            when (t) {
                is MutableList<*> ->
                    t.filterNotNull().forEach {
                        uniquesByParent(it, uniquesByParent)
                    }
                else ->
                    getChildren(t).also {
                        uniquesByParent[t] = it
                        uniquesByParent(it, uniquesByParent)
                    }
            }
            return uniquesByParent
        }

        private fun getChildren(obj: Any): List<Any> {
            val fields = FieldCache.fieldsFor(obj)
            val customs = fields.customs
                    .map { fieldGet(it, obj) }
            val listsOfCustoms = fields.listsOfCustoms
                    .map { fieldGet(it, obj) }
                    .flatten()
                    .filterNotNull()
                    .distinctBy(UidBuilder::buildUniqueId)

            return customs + listsOfCustoms
        }
    }
}