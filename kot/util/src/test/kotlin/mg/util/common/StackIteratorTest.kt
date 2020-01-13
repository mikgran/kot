package mg.util.common

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StackIteratorTest {

    @Test
    fun testHasNext() {
    }


    @Test
    fun testNext() {
    }

    @Test
    fun testPlus() {
    }

    @Test
    fun testStackIterator() {

        val list1 = listOf(1, 2, 3, 4, 5)
        val list2 = listOf(6, 7, 8, 9, 10)
        val list3 = listOf(11)

        val stackIterator: StackIterator<Int> = list1.stackIterator() + list2 + list3

        stackIterator.forEach {
            println(it)
        }



    }
}