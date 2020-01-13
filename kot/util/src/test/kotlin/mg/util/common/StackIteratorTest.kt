package mg.util.common

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StackIteratorTest {

    @Test
    fun testHasNext() {
        // TOIMPROVE: test coverage
    }


    @Test
    fun testNext() {
    }

    @Test
    fun testPlus() {
    }

    @Test
    fun testStackIterator1() {

        val list1 = listOf(1, 2, 3, 4, 5)
        val list2 = listOf(6, 7)
        val list3 = listOf(8, 9, 10)
        val list4 = listOf(11)

        val stackIterator: StackIterator<Int> = list1.stackIterator() + list2.iterator() + list3.iterator() + list4.iterator()

        val candidateList = mutableListOf<Int>()

        stackIterator.forEach { candidateList.add(it) }

        assertEquals((1..11).toList(), candidateList)
    }
}