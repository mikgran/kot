package mg.util.common

import mg.util.common.FunctionComposition.Companion.plus
import mg.util.common.PredicateComposition.Companion.and
import mg.util.common.PredicateComposition.Companion.not
import mg.util.common.PredicateComposition.Companion.or
import mg.util.common.PredicateComposition.Companion.rangeTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class FunctionCompositionTest {

    @Test
    fun test_FunctionAndPredicateComposition() {

        fun same(value: Int): Int = value
        fun twice(value: Int): Int = value * 2
        fun thrice(value: Int): Int = value * 3

        fun isLengthLessThanTen(s: String): Boolean = s.length < 10
        fun isAContained(s: String): Boolean = s.contains("a")
        fun isBContained(s: String): Boolean = s.contains("b")

        fun isLengthLessThanTenOrIsAContained(s: String) = (::isLengthLessThanTen or ::isAContained)(s)
        assertFalse(isLengthLessThanTenOrIsAContained("bbbbbbbbbb"))
        assertTrue(isLengthLessThanTenOrIsAContained("bbbbbbbbba"))
        assertTrue(isLengthLessThanTenOrIsAContained("bbbbbbb"))

        fun isAContainedAndLengthLessThanTen(s: String) = (::isLengthLessThanTen..::isAContained)(s)
        fun isAContainedAndLengthLessThanTenB(s: String) = (::isLengthLessThanTen and ::isAContained)(s)
        assertTrue(isAContainedAndLengthLessThanTen("a"))
        assertTrue(isAContainedAndLengthLessThanTenB("a"))
        assertFalse(isAContainedAndLengthLessThanTen("abccbbccbb"))
        assertFalse(isAContainedAndLengthLessThanTenB("abccbbccbb"))

        fun multiplyBy6(i: Int) = (::same + ::twice + ::thrice)(i)
        fun multiplyBy2(i: Int) = (::same + ::twice)(i)
        assertEquals(6, multiplyBy6(1))
        assertEquals(2, multiplyBy2(1))

        fun isNotAContained(s: String) = (!::isAContained)(s)
        assertTrue(isNotAContained("bbb"))
        assertFalse(isNotAContained("aaa"))

        fun isNotAAndBContained(s: String) = (!::isAContained and ::isBContained)(s)
        assertTrue(isNotAAndBContained("bbb"))
        assertFalse(isNotAAndBContained("ab"))
        assertFalse(isNotAAndBContained("aaa"))
    }
}