package mg.util.db

import mg.util.db.dsl.mysql.BuildingBlock

interface DslMapper {

    // Free hand DSL
    // val sql = Sql select PersonB() where PersonB::firstName eq "name"
    // MySqlDslMapper.map(sql.list()) ->
    // SELECT * FROM person12345 as p WHERE p.firstName = "name"

    fun map(blockList: MutableList<BuildingBlock>): String
}