package mg.util.functional

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Opt3Test {

    @Test
    fun testGet() {

        Opt3.of(VALUE)
                .get()
                .let {
                    assertNotNull(it)
                    assertEquals(VALUE, it)
                }
    }

    @Test
    fun testWith() {
        Opt3.of(VALUE)
                .use {
                    assertEquals(VALUE, it)
                }
    }

    @Test
    fun testMap() {

        Opt3.of(VALUE)
                .map { a -> a + a }
                .use {
                    assertNotNull(it)
                    assertEquals(VALUE + VALUE, it)
                }
    }

    @Test
    fun testMapTo() {

        Opt3.of(VALUE)
                .mapTo { a -> a + a }
                .let {
                    assertNotNull(it)
                    assertEquals(VALUE + VALUE, it)
                }

        Opt3.of(1)
                .mapTo { i -> i + i }
                .let {
                    assertNotNull(it)
                    assertEquals(2, it)
                }
    }

    @Test
    fun testUse() {

        Opt3.of(VALUE)
                .use {
                    assertNotNull(it)
                    assertEquals(VALUE, it)
                }
    }

    @Test
    fun testSupply() {

        Opt3.empty<String>()
                .use { assertNull(it) }
                .supply { VALUE }
                .use {
                    assertNotNull(it)
                    assertEquals(VALUE, it)
                }
    }

    @Test
    fun testGetOrElse() {

        Opt3.empty<String>()
                .use { assertNull(it) }
                .getOrElse(VALUE)
                .let { assertEquals(VALUE, it) }

        Opt3.of(VALUE)
                .use { assertNotNull(it) }
                .getOrElse(VALUE2)
                .let { assertEquals(VALUE, it) }
    }

    @Test
    fun testElseThrow() {

        assertThrows(Exception::class.java) {
            Opt3.empty<String>()
                    .elseThrow { Exception() }
        }

        assertDoesNotThrow {
            Opt3.of(VALUE)
                    .elseThrow { Exception() }
        }
    }

    companion object {
        const val VALUE = "value"
        const val VALUE2 = "value2"
    }


}