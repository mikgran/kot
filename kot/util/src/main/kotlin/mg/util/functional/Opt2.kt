package mg.util.functional

class Opt2<T> {

    /*
        The lazyT should be operated behind an isPresent() null check always,
        therefore value!! will never throw an exception.
        The cost is a lot slower eval with mapper(lazyT) than mapper(value).
    */
    private val lazyT: T by lazy { value!! }

    private var value: T? = null

    constructor()

    constructor(t: T?) {
        value = t
    }

    private fun isPresent(): Boolean = value != null

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

    override fun toString(): String = value?.toString() ?: ""

    companion object Factory {

        @JvmStatic
        fun <T> of(t: T): Opt2<T> = when (t) {
            null -> empty()
            else -> Opt2(t)
        }

        @JvmStatic
        fun <T> of(t: Opt2<T>): Opt2<T> = when {
            t.isPresent() -> Opt2(t.value)
            else -> empty()
        }

        @JvmStatic
        fun <T> empty(): Opt2<T> = Opt2()
    }
}