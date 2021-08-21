package mg.util.functional

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

class Opt2<T : Any> {

    private var value: T? = null

    constructor()

    constructor(t: T?) {
        value = t
    }

    fun isPresent(): Boolean = value != null
    fun get() = value
    fun value(): T = value!!

    fun <R : Any> map(mapper: (T) -> R?): Opt2<R> = when {
        isPresent() -> of(mapper(value!!))
        else -> empty()
    }

    fun ifEmpty(supplier: () -> T): Opt2<T> = when {
        !isPresent() -> Opt2(supplier())
        else -> this
    }

    fun ifEmptyUse(conditionalSupplier: (() -> T)?): Opt2<T> = when {
        of(conditionalSupplier).isPresent() && !isPresent() -> Opt2(conditionalSupplier?.invoke())
        else -> this
    }

    fun ifMissing(block: () -> Unit): Opt2<T> {
        if (!isPresent()) {
            block()
        }
        return this
    }

    fun ifMissingThrow(exceptionSupplier: () -> Throwable): Opt2<T> {
        if (!isPresent()) {
            throw exceptionSupplier()
        }
        return this
    }

    fun filter(predicate: (T) -> Boolean): Opt2<T> = when {
        isPresent() && predicate(value!!) -> this
        else -> empty()
    }

    fun filterNot(predicate: (T) -> Boolean): Opt2<T> = when {
        isPresent() && !predicate(value!!) -> this
        else -> empty()
    }

    fun <V : Any> match(predicate: Boolean, mapper: (T) -> V): BiOpt2<T, V> =
            this.match({ predicate }, mapper)

    fun <V : Any> match(predicate: (T) -> Boolean, mapper: (T) -> V): BiOpt2<T, V> {
        return this.filter { isPresent() && predicate(value!!) }
                .map(mapper)
                .map { v -> BiOpt2.of(value!!, v) }
                .getOrElse(getBiOpt2OfValueAndEmpty())
    }

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, V : Any> match(
            ref: R,
            predicate: (R) -> Boolean,
            mapper: (R) -> V,
    ): BiOpt2<T, V> {

        // maps and filters only non null values of the same class.
        // returns BiOpt2.of(oldValue, newValue/null)
        return this.filter { isPresent() && isValueClassSameAsRefClass(ref) }
                .map { it as R }
                .filter(predicate)
                .map(mapper)
                .map { v -> BiOpt2.of(value!!, v) }
                .getOrElse(getBiOpt2OfValueAndEmpty())
    }

    private fun <V : Any> getBiOpt2OfValueAndEmpty(): BiOpt2<T, V> = BiOpt2.of(of(value), empty())

    fun <V : Any> case(
            predicate: (T) -> Boolean,
            mapper: (T) -> V,
    ): BiOpt2<T, V> {

        return this.filter { isPresent() && predicate(value!!) }
                .map(mapper)
                .map { newRight -> BiOpt2.of(value!!, newRight) }
                .getOrElse(getBiOpt2OfValueAndEmpty())
    }

    private fun <R : Any> isValueClassSameAsRefClass(ref: R): Boolean =
            value?.let { it::class == ref::class } ?: false

    fun getOrElse(default: T): T = when {
        isPresent() -> value!!
        else -> default
    }

    fun getOrElse(defaultProducer: () -> T) = when {
        isPresent() -> value!!
        else -> defaultProducer()
    }

    fun <R : Any> getAndMap(mapper: (T) -> R): R? = when {
        isPresent() -> mapper(value!!)
        else -> null
    }

    fun getOrElseThrow(exceptionProducer: () -> Throwable): T? = when {
        isPresent() -> value
        else -> throw exceptionProducer()
    }

    fun ifPresent(consumer: (T) -> Unit): Opt2<T> {
        if (isPresent()) {
            consumer(value!!)
        }
        return this
    }

    fun <R : Any> ifPresentWith(r: Opt2<R>, consumer: (T, R) -> Unit): Opt2<T> {
        if (isPresent() && r.isPresent()) {
            consumer(value!!, r.value!!)
        }
        return this
    }

    fun c(consumer: (T) -> Unit): Opt2<T> = ifPresent(consumer)

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

    fun <R : Any, V : Any> mapWith(r: R?, mapper: (T, R) -> V): Opt2<V> {
        val rOpt = of(r)
        return when {
            isPresent() && rOpt.isPresent() -> of(mapper(value!!, rOpt.value!!))
            else -> empty()
        }
    }

    // TOIMPROVE: find a better way for arity of type N objects
    fun <R : Any, S : Any, V : Any> mapWith(r: R?, s: S?, mapper: (T, R, S) -> V): Opt2<V> {
        val rOpt = of(r)
        val sOpt = of(s)
        return when {
            isPresent() && rOpt.isPresent() && sOpt.isPresent() -> of(mapper(value!!, rOpt.value!!, sOpt.value!!))
            else -> empty()
        }
    }

    fun <V : Any> mapTo(toType: KClass<V>): Opt2<V> = when {
        isPresent() -> toType.safeCast(value!!).toOpt()
        else -> empty()
    }

    fun <R : Any> xmap(extensionMapper: T.() -> R): Opt2<R> = when {
        isPresent() -> of(value!!.extensionMapper())
        else -> empty()
    }

    // synonymous with(value!!) { this.extensionMapper() }
    // is a renamed also() to avoid name clash
    fun x(extensionConsumer: T.() -> Unit): Opt2<T> {
        if (isPresent()) {
            value!!.extensionConsumer()
        }
        return this
    }

    inline fun <reified V : Any, R : Any> map(type: Iterator<*>, mapper: (V) -> R): Opt2<List<R>> {
        val list = mutableListOf<R>()
        for (element in type) if (element is V) list += mapper(element)
        return of(list)
    }

    inline fun <reified V : Any, R : Any> lmap(mapper: (V) -> R): Opt2<List<R>> {
        return when (val type = get()) {
            is List<*> -> map(type.iterator(), mapper)
            is Iterator<*> -> map(type, mapper)
            else -> empty()
        }
    }

    fun <V : Any> mapIf(conditionalMapper: (T) -> V): Opt2<V> {
        return when {
            isPresent() && value is Boolean && value == true -> conditionalMapper(value!!).toOpt()
            else -> empty()
        }
    }

    fun <V : Any> mapIfNot(conditionalMapper: (T) -> V): Opt2<V> {
        return when {
            isPresent() && value is Boolean && value == false -> conditionalMapper(value!!).toOpt()
            else -> empty()
        }
    }

    fun <V : Any> mapIf(predicate: Boolean, conditionalMapper: (T) -> V): Opt2<V> {
        return when {
            isPresent() && predicate -> conditionalMapper(value!!).toOpt()
            else -> empty()
        }
    }

    inline fun <reified V : Any> mapWhen(predicate: Boolean, conditionalMapper: (T) -> V): Opt2<V> {
        return when {
            isPresent() && predicate -> conditionalMapper(value()).toOpt()
            isPresent() && value() is V -> (get() as? V).toOpt()
            else -> empty()
        }
    }

    inline fun <reified V : Any> mapWhen(predicateFunction: (T) -> Boolean, conditionalMapper: ((T) -> V)): Opt2<V> {
        return when {
            isPresent() && predicateFunction(value()) -> conditionalMapper(value()).toOpt()
            isPresent() && value() is V -> (get() as? V).toOpt()
            else -> empty()
        }
    }

    /**
     * A non transforming through-to-list-for-each (lxforEach). Performs side-effect
     * on the contents, with no modification of the contents.
     */
    inline fun <reified V : Any> lxforEach(consumer: (V) -> Unit): Opt2<List<V>> {
        val list = toList<V>()
        list.forEach(consumer)
        return list.toOpt()
    }

    inline fun <reified V : Any, R : Any> lxmap(mapper: List<V>.() -> List<R>): Opt2<List<R>> = of(toList<V>().mapper())
    inline fun <reified V : Any> lfilter(predicate: (V) -> Boolean): Opt2<List<V>> = of(toList<V>().filter(predicate))
    inline fun <reified V : Any> toList(): List<V> {
        return when (val value = get()) {
            is List<*> -> value.filterIsInstance<V>()
            is V -> listOf(value)
            else -> emptyList()
        }
    }

    companion object Factory {

        @JvmStatic
        fun <T : Any> of(t: T?): Opt2<T> = when (t) {
            null -> empty()
            else -> Opt2(t)
        }

        @JvmStatic
        fun <T : Any> of(t: Opt2<T>): Opt2<T> = when {
            t.isPresent() -> Opt2(t.value!!)
            else -> empty()
        }

        @JvmStatic
        fun <T : Any> empty(): Opt2<T> = Opt2()
    }
}

fun <T : Any> T?.toOpt(): Opt2<T> = Opt2.of(this)

fun <T : Any> Boolean?.mapIf(conditionalMapper: Boolean.() -> T): Opt2<T> =
        this.toOpt().mapIf(conditionalMapper)

fun <T : Any> Boolean?.mapIfNot(conditionalMapper: Boolean.() -> T): Opt2<T> =
        this.toOpt().mapIfNot(conditionalMapper)