package mg.kot.rgen

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

fun main(args: Array<String>) {

    val server = Server(8080)
    val ctx = ServletContextHandler(ServletContextHandler.NO_SESSIONS)

    ctx.setContextPath("/")
    server.setHandler(ctx)

    val servletHolder = ServletHolder(ServletContainer(SimpleApplication()))

    // val serHol = ctx.addServlet(ServletContainer::class.java, "/rest/*")

    ctx.addServlet(servletHolder, "/*")
//    serHol.initOrder = 1
//    serHol.setInitParameter("jersey.config.server.provider.packages", "mg.kot.rgen")

    try {
        server.start()
        server.join()

    } catch (ex: Throwable) {
        // TOIMPROVE: logging
    } finally {
        server.destroy()
    }
}