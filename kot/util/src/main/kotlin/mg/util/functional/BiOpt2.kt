package mg.util.functional

class BiOpt2<T : Any, V : Any>(l: Opt2<T>, r: Opt2<V>) {

    private val left: Opt2<T> = l // original value
    private val right: Opt2<V> = r // transformed result

    fun left() = left
    fun right() = right

    @Suppress("unused")
    fun original() = left

    fun result() = right
    fun rightElseLeft() = if (right.isPresent()) right else left
    fun resultElseOrig() = rightElseLeft()

    fun <R : Any, V : Any> match(ref: R,
                                 predicate: (R) -> Boolean,
                                 mapper: (R) -> V): BiOpt2<T, V> {

        return left.match(ref, predicate, mapper)
    }

    fun <R : Any, V : Any> matchRight(ref: R,
                                      predicate: (R) -> Boolean,
                                      mapper: (R) -> V): BiOpt2<T, V> {

        val matchedRight = right.match(ref, predicate, mapper)

        @Suppress("UNCHECKED_CAST")
        return of(right, matchedRight.right()) as BiOpt2<T, V> // the new match situation is <old right, new right> type, force into T, V
    }

    fun case(predicate: (T) -> Boolean,
             mapper: (T) -> V): BiOpt2<T, V> {

        return this.filter { !right.isPresent() && left.isPresent() && predicate(left.get() as T) }
                .left()
                .map(mapper)
                .map { newRight -> BiOpt2(left, Opt2(newRight)) }
                .getOrElse(this)
    }

    fun caseDefault(mapper: (T) -> V) : BiOpt2<T, V> = when {
        left.isPresent() && !right.isPresent() -> of(left(), Opt2.of(mapper(left.get() as T)))
        else -> this
    }

    @Suppress("UNCHECKED_CAST")
    fun filter(filterFunction: (T) -> Boolean): BiOpt2<T, V> = when {
        left.isPresent() && filterFunction(left.get() as T) -> this
        else -> empty()
    }

    fun getLeftOrElseThrow(exceptionProducer: () -> Throwable): T? = when {
        left.isPresent() -> left.get()
        else -> throw exceptionProducer()
    }

    fun getRightOrElseThrow(exceptionProducer: () -> Throwable): V? = when {
        right.isPresent() -> right.get()
        else -> throw exceptionProducer()
    }

    fun toRight(): BiOpt2<T, T> {
        return when {
            left.isPresent() -> of(left(), left())
            else -> empty()
        }
    }

    fun toLeft(): BiOpt2<V, V> {
        return when {
            right().isPresent() -> of(right(), right())
            else -> empty()
        }
    }

    override fun toString(): String {
        return "($left, $right)"
    }

    companion object Factory {

        @JvmStatic
        fun <T : Any, V : Any> of(t: T?, v: V?): BiOpt2<T, V> = BiOpt2(Opt2.of(t), Opt2.of(v))

        @JvmStatic
        fun <T : Any, V : Any> of(t: Opt2<T>, v: Opt2<V>): BiOpt2<T, V> = when {
            t.isPresent() || v.isPresent() -> BiOpt2(t, v)
            else -> empty()
        }

        @JvmStatic
        fun <T : Any, V : Any> empty(): BiOpt2<T, V> = BiOpt2(Opt2.empty(), Opt2.empty())
    }

}