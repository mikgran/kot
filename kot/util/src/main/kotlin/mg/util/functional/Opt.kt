package mg.util.functional

class Opt<T>(v: T?) {

    private val value = v

    fun get() = value

    @Suppress("UNCHECKED_CAST")
    fun <R> map(mapper: (T?) -> R?): Opt<R?> {
        return if (isPresent()) {
            val newValue: R? = mapper(value)
            of(newValue)
        } else {
            EMPTY as Opt<R?>
        }
    }

    private fun isPresent(): Boolean = value != null

    fun ifEmpty(function: () -> T?): Opt<T?> {
        return of(function())
    }

    fun filter(filter: (T?) -> Boolean) : Opt<T?> {
        return when {
            filter(value) -> of(value)
            else -> EMPTY as Opt<T?>
        }
    }

    companion object Factory {

        private val EMPTY: Opt<*>? = Opt(null)

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> of(value: T?): Opt<T?> = if (value == null) EMPTY as Opt<T?> else Opt(value)

        @JvmStatic
        fun <T> of(value: Opt<T?>) = Opt(value.get())
    }
}

