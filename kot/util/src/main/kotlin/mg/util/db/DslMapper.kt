package mg.util.db

interface DslMapper {

    // Free hand DSL
    // val op = Sql select PersonB() where PersonB::firstName is "name"
    // MySqlDslMapper.map(op.list()) ->
    // SELECT * FROM person12345 as p WHERE p.firstName = "name"

    fun <T : Any> map(blockList: MutableList<BuildingBlock>): T
}