package mg.util.common

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StackIteratorTest {

    private val list1 = listOf(1, 2, 3, 4, 5)
    private val list2 = listOf(6, 7)
    private val list3 = listOf(8, 9, 10)
    private val list4 = listOf(11)

    @Test
    fun testHasNextAndNext() {

        val iterator = list2.stackIterator()

        assertTrue(iterator.hasNext())
        assertEquals(6, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(7, iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows(NoSuchElementException::class.java) {
            iterator.next()
        }
    }

    @Test
    fun testStackIterator() {

        testStackOperatorFor {
            list1.stackIterator() +
                    list2.iterator() +
                    list3.iterator() +
                    list4.iterator()
        }
    }

    @Test
    fun testStackIteratorWithOperatorPlus() {

        testStackOperatorFor {
            list1.iterator() +
                    list2.iterator() +
                    list3.iterator() +
                    list4.iterator()
        }
    }

    private fun testStackOperatorFor(stackIteratorSupplier: () -> StackIterator<Int>) {
        val candidateList = mutableListOf<Int>()
        stackIteratorSupplier().forEach { candidateList.add(it) }
        assertEquals((1..11).toList(), candidateList)
    }
}
