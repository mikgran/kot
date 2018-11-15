package mg.kot.rgen

import org.glassfish.jersey.server.ResourceConfig

class RgenApplication : ResourceConfig() {

    init {
        packages("mg.kot.rgen")
    }
}