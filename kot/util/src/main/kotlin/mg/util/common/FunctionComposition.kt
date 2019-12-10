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
        operator fun <T> ((T) -> Boolean).plus(other: (T) -> Boolean): (T) -> Boolean {
            return {
                this(it) && other(it)
            }
        }
    }
}


