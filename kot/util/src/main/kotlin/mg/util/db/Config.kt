package mg.util.db

import java.io.FileInputStream
import java.util.*

open class Config {

    @Synchronized
    private fun <T> synchronizedWithProperties(block: (Properties?) -> T): T = block(properties)

    open fun loadProperties(): Properties {
        return loadProperties(CONFIG_PROPERTIES)
    }

    fun loadProperties(fileName: String): Properties {

        synchronizedWithProperties { p ->

            if (!isPropertiesLoaded) {
                FileInputStream(fileName).use { inputStream ->
                    p?.load(inputStream)
                }
                isPropertiesLoaded = true
            }
        }
        return properties
    }

    @Suppress("unused")
    fun refreshProperties(): Properties {
        isPropertiesLoaded = false
        return loadProperties()
    }

    @Suppress("unused")
    fun refreshProperties(file: String): Properties {
        isPropertiesLoaded = false
        return loadProperties(file)
    }

    companion object {
        const val CONFIG_PROPERTIES = "config.properties"

        private var isPropertiesLoaded = false
        private var properties = Properties()

    }
}

