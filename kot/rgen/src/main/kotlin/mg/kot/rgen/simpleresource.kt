package mg.kot.rgen

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("helloWorld")
class HelloWorldResource {
    @GET
    fun helloWorld() = "Hello World"
}