package mg.util.functional

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class Opt2Test {


    @Test
    fun testttt() {

        val s = Opt2.of("")
                .map { s -> "$s!" }
                .get()

        println("s: $s")

        val nullStr: String? = null

        val ss = Opt2.of(nullStr)
                .map { ns -> "$ns!" }
                .ifEmpty { "str" }
                .filter { ns -> ns.length == 2 }
                .get()

        println("ss: $ss")

        val sss = Opt2.of(10)
                .filter { it > 9 }
                .map { it + 1 }
                .get()

        println(sss)
    }

    @Test
    fun test_map() {

        val opt = Opt2.of(VALUE)
        val get = opt
                .map { s -> "$s!" }
                .get()

        assertNotNull(get)
        assertEquals("$VALUE!", get)
    }


    class TempValue(var a: String?)

    companion object {
        const val ANOTHER_STRING = "anotherString"
        const val NEW_STRING = "newString"
        const val VALUE = "value"
        const val VALUE1 = "value1"
        const val VALUE2 = "value2"
        const val VALUE3 = "value2"
    }


}