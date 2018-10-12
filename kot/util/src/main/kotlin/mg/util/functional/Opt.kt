package mg.util.functional

import java.util.*

class Opt<T>(v: T) {

    private val value: T? = v

    fun get() = value

    fun <R : Any> map(mapper: (T?) -> R?): Opt<R?> {
        @Suppress("UNCHECKED_CAST")
        return if (isPresent()) {
            val newValue: R? = mapper(value)
            of(newValue)
        } else {
            empty()
        }
    }

    fun filter(predicate: (T?) -> Boolean): Opt<T?> {

        return if (predicate(value)) {
            of(value)
        } else {
            empty()
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

    private fun <R : Any> isValueClassSameAsRefClass(ref: R): Boolean {

        // no reified
        return value?.let {
            val valueAsAny = it as Any
            valueAsAny::class == ref::class
        } ?: false
    }

    /**
     * Performs a mapper function against contents of this Opt if the filter
     * returns true and there are contents.
     */
    fun <R : Any, V : Any> match(ref: R,
                                 filter: (R) -> Boolean,
                                 mapper: (R) -> V?): Opt<V?> {

        // maps only non null values of the same class
        @Suppress("UNCHECKED_CAST")
        return if (isPresent() &&
                isValueClassSameAsRefClass(ref) &&
                filter(value as R)) {

            of(mapper(value as R))
        } else {
            empty()
        }
    }

    fun ifPresent(f: (T?) -> Unit) {
        if (isPresent()) {
            f(value)
        }
    }

    fun <R> getAndMap(f: (T?) -> R?): R? {
        return when {
            isPresent() -> f(value)
            else -> empty<R>().get()
        }
    }

    fun getOrElse(default: T): T {
        return value ?: default
    }

    companion object Factory {

        @JvmStatic
        fun <T> of(t: T?): Opt<T?> = when (t) {
            null -> empty()
            else -> Opt(t)
        }

        @JvmStatic
        fun <T> of(t: Opt<T?>): Opt<T?> = when {
            t.isPresent() -> t
            else -> empty()
        }

        @JvmStatic
        fun <T> empty(): Opt<T?> = Opt(null)
    }
}

