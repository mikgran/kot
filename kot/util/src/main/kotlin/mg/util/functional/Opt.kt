package mg.util.functional

class Opt<out T>(v: T?) {

    private var value = v

    fun get() = value

    companion object Factory {

        private val EMPTY: Opt<*>? = Opt(null)

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> of(value: T): Opt<T> = if (value == null) EMPTY as Opt<T> else Opt(value)

        @JvmStatic
        fun <T> of(value: Opt<T>) = Opt(value.get())
    }
}

