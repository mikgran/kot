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
