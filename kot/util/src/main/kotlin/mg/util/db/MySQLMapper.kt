package mg.util.db

// mysql dialect object to sql mapper
object MySQLMapper : SqlMapper {

    override fun <T : Any> buildFind(metadata: Metadata<T>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
1    }

    override fun <T : Any> buildInsert(metadata: Metadata<T>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> buildCreateTable(metadata: Metadata<T>): String {

        // create table
        // no-op with all good
        // throws Exception / re-throws SQL-exception if something goes wrong

        return ""
    }

    // TOIMPROVE: create direct sql mappers too?
    fun <T : Any> create(t: T): String {

        return t.toString()
    }

    fun <T : Any> find(t: T): String {

        return t.toString()
    }
}