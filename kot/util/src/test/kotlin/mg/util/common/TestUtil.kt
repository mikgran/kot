package mg.util.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class TestUtil {

    companion object {

        // ref: https://en.wikipedia.org/wiki/Longest_common_substring_problem
        private fun longestCommonSubstring(s: String, t: String): MutableSet<String> {
            val table = Array(s.length) { IntArray(t.length) }
            var longest = 0
            val result: MutableSet<String> = HashSet()
            for (i in s.indices) {
                for (j in t.indices) {
                    if (s[i] != t[j]) {
                        continue
                    }
                    table[i][j] = if (i == 0 || j == 0) 1 else 1 + table[i - 1][j - 1]
                    if (table[i][j] > longest) {
                        longest = table[i][j]
                        result.clear()
                    }
                    if (table[i][j] == longest) {
                        result.add(s.substring(i - longest + 1, i + 1))
                    }
                }
            }
            return result
        }

        fun <T : Any> expect(expected: T, candidate: T?) {
            assertNotNull(candidate)
            if (expected != candidate) {
                println("\nE:\n<$expected>")
                println("C:\n<$candidate>")
                if (expected is String && candidate is String) {
                    val common = longestCommonSubstring(expected, candidate)
                    if (common.size > 0) {
                        println("\nLongest common part:\n<${common.first()}>")
                    }
                }
            }
            assertEquals(expected, candidate)
        }

        fun expect(expected: String, candidate: String?, splitDelimeter: String = ";") {
            assertNotNull(candidate)
            if (candidate != null && expected != candidate) {
                val splitExpected = expected.split(splitDelimeter).joinToString("\n")
                val splitCandidate = candidate.split(splitDelimeter).joinToString("\n")
                println("\nE:\n<$splitExpected>")
                println("C:\n<$splitCandidate>")
                val common = longestCommonSubstring(expected, candidate)
                if (common.size > 0) {
                    println("\nLongest common part:\n<${common.first().split(splitDelimeter).joinToString("\n")}>")
                }
            }
            assertEquals(expected, candidate)
        }
    }
}
