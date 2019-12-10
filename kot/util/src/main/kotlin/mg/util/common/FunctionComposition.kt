package mg.util.common

class FunctionComposition {
    companion object {
        operator fun <T, R, V> ((T) -> R).plus(other: (R) -> V): ((T) -> V) {
            return {
                other(this(it))
            }
        }
    }
}

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

