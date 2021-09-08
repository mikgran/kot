package mg.util.db

import mg.util.common.Common.classSimpleName
import mg.util.common.TestUtil
import mg.util.db.TestDataClasses.*
import org.junit.jupiter.api.Test

internal class TableDropTest {

    @Test
    fun testRegister() {

        val cleaner = TableDrop()
        val tdMultipleComposition = TDMultipleComposition()

        val expectedTables = mutableListOf(
                TDMultipleComposition(),
                TDSimple(),
                TDSimpleComp(),
                TDSubComp(),
        )
        val expectedJoinTables = mutableListOf(
                TDMultipleComposition() to TDSimple(),
                TDMultipleComposition() to TDSimpleComp(),
                TDSimpleComp() to TDSubComp(),
        )
        expectedTables.sortBy { it.classSimpleName() }
        expectedJoinTables.sortBy { it.first.classSimpleName() + it.second.classSimpleName() }

        cleaner.registerRelational(tdMultipleComposition)

        TestUtil.expect(expectedTables, cleaner.contentsTables())
        TestUtil.expect(expectedJoinTables, cleaner.contentsJoinTables())
    }
}