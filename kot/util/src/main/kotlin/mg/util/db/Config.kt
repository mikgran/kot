package mg.util.db

import java.io.FileInputStream
import java.util.*

open class Config {

    private var properties: Properties? = null

    open fun loadProperties(): Properties {
        return loadProperties(CONFIG_PROPERTIES)
    }

    fun loadProperties(fileName: String): Properties {

        if (properties == null) {
            properties = Properties()

            FileInputStream(fileName).use { inputStream ->

                properties?.load(inputStream)
            }
        }

        return properties!!
    }

    fun refreshProperties(): Properties {
        properties = null
        return loadProperties()
    }

    fun refreshProperties(file: String): Properties {
        properties = null
        return loadProperties(file)
    }

    companion object {
        const val CONFIG_PROPERTIES = "config.properties"
    }
}

