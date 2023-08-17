package mg.util.functional

import java.util.*
import java.util.Collections.emptyList
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

class Opt3<T : Any> {

    private var value: T? = null

    constructor()
    constructor(t: T?) {
        value = t
    }

    fun get() = value

    fun <R : Any> map(mapper: (T) -> R?): Opt3<R> = when {
        value != null -> of(mapper(value!!))
        else -> empty()
    }

    fun <R : Any> mapTo(mapper: (T) -> R): R? = when {
        value != null -> mapper(value!!)
        else -> null
    }

    fun use(consumer: (t: T?) -> Unit): Opt3<T> {
        if (value != null) {
            consumer(value)
        }
        return this
    }

    fun supply(supplier: () -> T): Opt3<T> = when {
        value == null -> Opt3(supplier())
        else -> this
    }

    fun getOrElse(default: T): T = when {
        value != null -> value!!
        else -> default
    }

    fun elseThrow(exceptionSupplier: () -> Throwable): Opt3<T> {
        if (value == null) {
            throw exceptionSupplier()
        }
        return this
    }

    fun keep(predicate: (T) -> Boolean): Opt3<T> = when {
        value != null && predicate(value!!) -> this
        else -> empty()
    }

    fun lose(predicate: (T) -> Boolean): Opt3<T> = when {
        value != null && !predicate(value!!) -> this
        else -> empty()
    }

    private fun <R : Any> isValueClassSameAsRefClass(ref: R): Boolean =
            value?.let { it::class == ref::class } ?: false

    fun getOrElse(defaultProducer: () -> T) = when {
        value != null -> value!!
        else -> defaultProducer()
    }

    fun getOrThrow(exceptionProducer: () -> Throwable): T? = when {
        value != null -> value
        else -> throw exceptionProducer()
    }

    private fun consume(consumer: (T) -> Unit): Opt3<T> {
        if (value != null) {
            consumer(value!!)
        }
        return this
    }

    override fun toString(): String = value?.toString() ?: ""

    override fun equals(other: Any?): Boolean {

        return when (other) {
            !is Opt3<*> -> false
            else -> {
                val otherObj: Opt3<*> = other
                value == otherObj.value
            }
        }
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    fun <R : Any, V : Any> mapWith(r: R?, mapper: (T, R) -> V): Opt3<V> {
        val rOpt = of(r)
        return when {
            value != null && rOpt.value != null -> of(mapper(value!!, rOpt.value!!))
            else -> empty()
        }
    }

    fun <V : Any> toType(toType: KClass<V>): Opt3<V> = when {
        value != null -> toType.safeCast(value!!).toOpt3()
        else -> empty()
    }

    // synonymous with(value!!) { this.extensionMapper() }
    // is a renamed also() to avoid name clash
    fun with(extensionConsumer: T.() -> Unit): Opt3<T> {
        if (value != null) {
            value!!.extensionConsumer()
        }
        return this
    }


    inline fun <reified V : Any> toList(): List<V> =
            when (val value = get()) {
                is List<*> -> value.filterIsInstance<V>()
                is V -> listOf(value)
                else -> emptyList()
            }

    companion object Factory {

        @JvmStatic
        fun <T : Any> of(t: T?): Opt3<T> = when (t) {
            null -> empty()
            else -> Opt3(t)
        }

        @JvmStatic
        fun <T : Any> of(t: Opt3<T>): Opt3<T> = when {
            t.value != null -> Opt3(t.value!!)
            else -> empty()
        }

        @JvmStatic
        fun <T : Any> empty(): Opt3<T> = Opt3()
    }
}

fun <T : Any> T?.toOpt3(): Opt3<T> = Opt3.of(this)

