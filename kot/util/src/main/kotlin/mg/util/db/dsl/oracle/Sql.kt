package mg.util.db.dsl.oracle

import mg.util.db.dsl.mysql.Sql as Mysql

class Sql : Mysql() {

    override infix fun <T : Any> select(t: T): SelectBlock<T> = newListAndCacheBlock { list -> SelectBlock(list, t) }
    override infix fun <T : Any> update(t: T): UpdateBlock<T> = newListAndCacheBlock { list -> UpdateBlock(list, t) }

    companion object {
        infix fun <T : Any> select(t: T): SelectBlock<T> = Sql().select(t)
        infix fun <T : Any> update(t: T): UpdateBlock<T> = Sql().update(t)
    }
}