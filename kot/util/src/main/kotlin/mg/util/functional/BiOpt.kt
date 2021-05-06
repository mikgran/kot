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

    fun caseOf(predicate: (T?) -> Boolean,
               mapper: (T?) -> V?): BiOpt<T, V> {

        if (!right.isPresent() &&
                left.isPresent() &&
                predicate(left.get())) {

            val newRight: V? = mapper(left.get())
            val oldLeft: T? = left.get()

            val newBiOpt: BiOpt<out T, out V> = of(oldLeft, newRight)


            return BiOpt.Factory.of(
                    oldLeft,
                    newRight)
        }

        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun filter(filter: (T) -> Boolean): BiOpt<T, V> = when {

        left.isPresent() && filter(left.get() as T) -> this
        else -> BiOpt.empty()
    }

    fun getLeftOrElseThrow(exceptionProducer: () -> Throwable): T? {
        return when {
            left.isPresent() -> left.get()
            else -> throw exceptionProducer()
        }
    }

    fun getRightOrElseThrow(exceptionProducer: () -> Throwable): V? {
        return when {
            right.isPresent() -> right.get()
            else -> throw exceptionProducer()
        }
    }

    companion object Factory {

        fun <T, V> of(t: T?, v: V?): BiOpt<T, V> = when (t) {
                null -> empty()
                else -> BiOpt(Opt.of(t), Opt.of(v))
            }

        fun <T, V> of(t: Opt<T?>, v: Opt<V?>): BiOpt<T, V> = when {
                t.isPresent() || v.isPresent() -> BiOpt(t, v)
                else -> empty()
            }

        @JvmStatic
        fun <T, V> empty() = BiOpt(Opt.empty<T>(), Opt.empty<V>())
    }

}