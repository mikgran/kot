package mg.kot.rgen

import org.glassfish.jersey.server.ResourceConfig

class SimpleApplication : ResourceConfig() {
    init {
        packages("mg.kot.rgen")
    }
}