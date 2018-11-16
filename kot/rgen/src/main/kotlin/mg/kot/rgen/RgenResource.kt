package mg.kot.rgen

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("rgen")
class RgenResource {
    @GET
    fun helloWorld() = "Hello World"

// localhost:8080/rgen/<classid>  -> CRUD
}