package mg.util.functional

import java.util.*

class Opt2<T : Any> {

    /*
        The lazyT should be operated behind an isPresent() null check always,
        therefore value!! will never throw an exception.
        The cost is a lot slower eval with mapper(lazyT) than with mapper(value).
    */
    private val lazyT: T by lazy { value!! }
    private var value: T? = null

    constructor()

    constructor(t: T?) {
        value = t
    }

    fun isPresent(): Boolean = value != null

    fun get() = value

    fun <R : Any> map(mapper: (T) -> R): Opt2<R> = when {
        isPresent() -> Opt2.of(mapper(lazyT))
        else -> empty()
    }

    fun ifEmpty(supplier: () -> T): Opt2<T> = when {
        !isPresent() -> Opt2(supplier())
        else -> this
    }

    fun filter(predicate: (T) -> Boolean): Opt2<T> = when {
        isPresent() && predicate(lazyT) -> this
        else -> empty()
    }

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, V : Any> match(ref: R,
                                 predicate: (R) -> Boolean,
                                 mapper: (R) -> V): BiOpt2<T, V> {

        // maps and filters only non null values of the same class.
        // returns BiOpt.of(oldValue, newValue/null)
        return this.filter { isPresent() && isValueClassSameAsRefClass(ref) }
                .map { it as R }
                .filter(predicate)
                .map(mapper)
                .map { v -> BiOpt2.of(lazyT, v) }
                .getOrElse(getBiOpt2OfValueAndEmpty())
    }

    private fun <V : Any> getBiOpt2OfValueAndEmpty(): BiOpt2<T, V> = BiOpt2.of(of(value), empty())

    fun <V : Any> case(predicate: (T) -> Boolean,
                       mapper: (T) -> V): BiOpt2<T, V> {

        return this.filter { isPresent() && predicate(lazyT) }
                .map(mapper)
                .map { newRight -> BiOpt2.of(lazyT, newRight) }
                .getOrElse(getBiOpt2OfValueAndEmpty())
    }

    private fun <R : Any> isValueClassSameAsRefClass(ref: R): Boolean {

        // no reified
        return value?.let {
            val valueAsAny = it as Any
            valueAsAny::class == ref::class
        } ?: false
    }

    fun getOrElse(default: T): T = when {
        isPresent() -> lazyT
        else -> default
    }

    fun <R : Any> getAndMap(mapper: (T) -> R): R? = when {
        isPresent() -> mapper(lazyT)
        else -> null
    }

    fun getOrElseThrow(exceptionProducer: () -> Throwable): T? = when {
        isPresent() -> value
        else -> throw exceptionProducer()
    }

    fun ifPresent(consumer: (T) -> Unit): Opt2<T> {
        if (isPresent()) {
            consumer(lazyT)
        }
        return this
    }

    override fun toString(): String = value?.toString() ?: ""

    override fun equals(other: Any?): Boolean {

        return when (other) {
            !is Opt2<*> -> false
            else -> {
                val otherObj: Opt2<*> = other
                value == otherObj.value
            }
        }
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    companion object Factory {

        @JvmStatic
        fun <T : Any> of(t: T?): Opt2<T> = when (t) {
            null -> empty()
            else -> Opt2(t)
        }

        @JvmStatic
        fun <T : Any> of(t: Opt2<T>): Opt2<T> = when {
            t.isPresent() -> Opt2(t.lazyT)
            else -> empty()
        }

        @JvmStatic
        fun <T : Any> empty(): Opt2<T> = Opt2()
    }
}