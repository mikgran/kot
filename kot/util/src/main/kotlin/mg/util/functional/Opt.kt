package mg.util.functional

import java.util.*

class Opt<T>(v: T?) {

    // match
    // ifEmpty
    // ifPresent () -> T?
    // caseOf

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

    @Suppress("UNCHECKED_CAST")
    fun filter(filter: (T?) -> Boolean): Opt<T?> {
        return when {
            filter(value) -> of(value)
            else -> EMPTY as Opt<T?>
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

    fun match(function: (T) -> Boolean) : Opt<T?> {

        value?.let {
            if(function(it)) {

            }
        }

        @Suppress("UNCHECKED_CAST")
        return EMPTY as Opt<T?>
    }

    companion object Factory {

        private val EMPTY: Opt<*> = Opt(null)

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> of(value: T?): Opt<T?> = if (value == null) EMPTY as Opt<T?> else Opt(value)

        @JvmStatic
        fun <T> of(value: Opt<T?>) = Opt(value.get())
    }


}

