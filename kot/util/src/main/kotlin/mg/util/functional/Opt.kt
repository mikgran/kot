package mg.util.functional

import java.util.*

class Opt<out T>(v: T) {

    // match
    // ifEmpty
    // ifPresent () -> T non Any
    // caseOf

    private val value: T? = v

    fun get() = value

    @Suppress("UNCHECKED_CAST")
    fun <R : Any> map(mapper: (T?) -> R?): Opt<R?> {
        return if (isPresent()) {
            val newValue: R? = mapper(value)
            of(newValue)
        } else {
            EMPTY
        }
    }

    // @Suppress("UNCHECKED_CAST")
    fun filter(filterFunction: (T?) -> Boolean): Opt<T?> {

        return if (filterFunction(value)) {
            of(value)
        } else {
            EMPTY
        }
    }

    private fun isPresent(): Boolean = value != null

    fun <T : Any> ifEmpty(function: () -> T?): Opt<T?> = of(function())

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Opt<*> -> false
            else -> {
                val otherObj: Opt<*> = other
                value == otherObj.value
            }
        }
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    fun <R : Any> isValueClassSameAsRefClass(ref: R): Boolean {

        value?.let {
            println("value  $value")
            println("this::class  ${this::class}")
            println("ref::class  ${ref::class}")

            it::class == ref::class } ?: false
        }

        return true
    }

    fun <R : Any, V : Any> match(ref: R,
                                 filter: (T?) -> Boolean,
                                 mapper: (T?) -> V?): Opt<V?> {

//        println("isPresent() ${isPresent()}")
//        println("isValueClassSameAsRefClass(ref) ${isValueClassSameAsRefClass(ref)}")
//        println("filter(value) ${filter(value)}")

        return if (isPresent() &&
                isValueClassSameAsRefClass(ref) &&
                filter(value)) {

            of(mapper(value))
        } else {
            EMPTY
        }
    }

    companion object Factory {

        private val EMPTY = Opt(null)

        @JvmStatic
        fun <T> of(t: T?): Opt<T?> = when (t) {
            null -> EMPTY
            else -> Opt(t)
        }

        @JvmStatic
        fun <T> of(t: Opt<T?>): Opt<T?> = when {
            t.isPresent() -> t
            else -> EMPTY
        }
    }


}

