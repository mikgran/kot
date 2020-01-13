package mg.util.common

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StackTest {

    @Test
    fun isEmpty() {

        val stack = Stack<String>()

        assertTrue(stack.isEmpty())

        stack.push(A)

        assertTrue(!stack.isEmpty())
        assertEquals(A, stack.peek())
    }

    @Test
    fun count() {

        val stack = Stack<String>()

        stack.push(A)

        assertEquals(1, stack.count())

        stack.push(A)

        assertEquals(2, stack.count())

        stack.pop()

        assertEquals(1, stack.count())
    }

    @Test
    fun pushAndPopAndPeek() {

        val stack = Stack<String>()

        assertNull(stack.peek())

        stack.push(A)

        assertNotNull(stack.peek())

        stack.push(A2)

        assertEquals(A2, stack.peek())

        stack.pop()

        assertEquals(A, stack.peek())
    }

    @Test
    fun testCollectionPush() {

        val list = listOf(A, A2, A3)
        val stack = Stack<String>()

        assertEquals(0, stack.count())

        stack.push(list)

        assertEquals(3, stack.count())
        assertEquals(A3, stack.pop())
        assertEquals(A2, stack.pop())
        assertEquals(A, stack.pop())
    }

    companion object {
        private const val A = "a"
        private const val A2 = A + A
        private const val A3 = A2 + A
    }
}