package mg.util.functional

class BiOpt<T, V>(l: Opt<T?>, r: Opt<V?>) {

    private val left: Opt<T?> = l
    private val right: Opt<V?> = r

    fun left() = left
    fun right() = right

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, V : Any> match(ref: R,
                                 filter: (R?) -> Boolean,
                                 mapper: (R?) -> V?): BiOpt<T, V> {

        return left.match(ref, filter, mapper) as BiOpt<T, V>
    }

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, V : Any> matchRight(ref: R,
                                      filter: (R?) -> Boolean,
                                      mapper: (R?) -> V?): BiOpt<T, V> {

        val matchedRight = right.match(ref, filter, mapper)

        return BiOpt.of(right, matchedRight.right()) as BiOpt<T, V>
    }

    companion object Factory {

        @JvmStatic
        fun <T, V> of(t: T?) = when (t) {
            null -> empty()
            else -> BiOpt(Opt.of(t), Opt.empty<V>())
        }

        @JvmStatic
        fun <T, V> of(t: T?, v: V?) = when (t) {
            null -> empty()
            else -> BiOpt(Opt.of(t), Opt.of(v))
        }

        @JvmStatic
        fun <T, V> of(t: Opt<T?>) = when {
            t.isPresent() -> BiOpt(t, Opt.empty<V>())
            else -> empty()
        }

        @JvmStatic
        fun <T, V> of(t: Opt<T?>, v: Opt<V?>) = when {
            t.isPresent() && v.isPresent() -> BiOpt(t, v)
            else -> empty()
        }

        @JvmStatic
        fun <T, V> empty() = BiOpt(Opt.empty<T>(), Opt.empty<V>())
    }

}