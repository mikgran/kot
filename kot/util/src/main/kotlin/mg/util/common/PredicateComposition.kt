package mg.util.common

class PredicateComposition {
    companion object {
        infix fun <T> ((T) -> Boolean).or(other: (T) -> Boolean): (T) -> Boolean {
            return {
                this(it) || other(it)
            }
        }

        operator fun <T> ((T) -> Boolean).rangeTo(other: (T) -> Boolean): (T) -> Boolean {
            return {
                this(it) && other(it)
            }
        }

        infix fun <T> ((T) -> Boolean).and(other: (T) -> Boolean): (T) -> Boolean {
            return {
                this(it) && other(it)
            }
        }

        operator fun <T> ((T) -> Boolean).not(): (T) -> Boolean = {
            !this(it)
        }
    }
}