package mg.util.common

class StackIterator<T> : Iterator<T> {

    private val stack = Stack<Iterator<T>>()

    override fun hasNext(): Boolean = when {
        stack.isEmpty() -> false
        else -> stack.peekNotNull().hasNext()
    }

    override fun next(): T {

        val t = stack.peekNotNull().next()

        if (stack.peek()?.hasNext() == false) {
            stack.pop()
        }

        return t
    }

    infix operator fun plus(t: Iterator<T>): StackIterator<T> {
        stack.push(t)
        return this
    }

    infix operator fun plus(t: Collection<T>): StackIterator<T> {
        stack.addTo(t.iterator(), 0)
        return this
    }
}

fun <T> Collection<T>.stackIterator(): StackIterator<T> = StackIterator<T>().also { it + this.iterator() }