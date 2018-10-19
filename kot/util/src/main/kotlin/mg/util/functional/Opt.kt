package mg.util.functional

import java.util.*

class Opt<T>(v: T?) {

    private val value: T? = v

    fun get() = value

    @Suppress("UNCHECKED_CAST")
    fun <R : Any> map(mapper: (T?) -> R?): Opt<R?> {
        return when {
            isPresent() -> {
                val newValue = mapper(value)
                of(newValue)
            }
            else -> empty()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun filter(predicate: (T?) -> Boolean): Opt<T?> {
        return when {
            isPresent() && predicate(value) -> thisAsOptNullableT()
            else -> empty()
        }
    }

    fun isPresent(): Boolean = value != null

    fun ifEmpty(function: () -> T): Opt<T?> {
        return when {
            !isPresent() -> Opt(function())
            else -> thisAsOptNullableT()
        }
    }

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

    // XXX: create BiOpt for tracking the original and the result values.
    // XXX: match { consumer }
    // XXX: match { func } : BiOpt

    /**
     * Performs a mapper function against contents of this Opt if the filter
     * returns true and there are contents.
     */
    @Suppress("UNCHECKED_CAST")
    fun <R : Any, V : Any> match(ref: R,
                                 filter: (R?) -> Boolean,
                                 mapper: (R?) -> V?): BiOpt<R, V> {

        // maps and filters only non null values of the same class.
        return this.filter { isPresent() }
                .filter { isValueClassSameAsRefClass(ref) }
                .map { v -> v as R }
                .filter { v -> filter(v) }
                .map { v -> mapper(v) }
                .map { v -> BiOpt.of(value, v) }
                .getOrElse(BiOpt.empty()) as BiOpt<R, V>
    }

    @Suppress("UNCHECKED_CAST")
    fun ifPresent(consumer: (T?) -> Unit): Opt<T?> {
        if (isPresent()) {
            consumer(value)
        }
        return thisAsOptNullableT()
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> getAndMap(mapper: (T?) -> R?): R? {
        return when {
            isPresent() -> mapper(value)
            else -> empty<R>().get()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getOrElse(default: T): T {
        return when {
            isPresent() -> value as T
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun thisAsOptNullableT() = this as Opt<T?>

    companion object Factory {

        @JvmStatic
        fun <T> of(t: T?): Opt<T?> = when (t) {
            null -> Opt(null)
            else -> Opt(t)
        }

        @JvmStatic
        fun <T> of(t: Opt<T?>): Opt<T?> = when {
            t.isPresent() -> Opt(t.value)
            else -> Opt(null)
        }

        @JvmStatic
        fun <T> empty(): Opt<T?> = Opt(null)
    }
}
