package mg.util.common

class StackIterator<T> : Iterator<T> {

    private val stack = Stack<Iterator<T>>()

    override fun hasNext(): Boolean = when {
        stack.isEmpty() -> false
        else -> stack.peekNotNull().hasNext()
    }

    override fun next(): T {
        val t = stack.peekNotNull().next()

        if (!stack.isEmpty() && !stack.peekNotNull().hasNext()) {
            stack.pop()
        }
        return t
    }

    operator fun plus(iterator: Iterator<T>): StackIterator<T> {
        if (iterator.hasNext()) {
            stack.add(iterator, 0)
        }
        return this
    }
}

fun <T> Collection<T>.stackIterator() = StackIterator<T>().also { it + this.iterator() }

operator fun <T> Iterator<T>.plus(iterator: Iterator<T>): StackIterator<T> =
        when {
            this is StackIterator<T> -> this.plus(iterator)
            iterator is StackIterator<T> -> iterator.plus(this)
            else -> StackIterator<T>().also { it.plus(this); it.plus(iterator) }
        }


