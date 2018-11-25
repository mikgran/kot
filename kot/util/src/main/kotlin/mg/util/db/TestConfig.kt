package mg.util.db

import mg.util.Common
import java.util.*

class TestConfig : Config() {

    private var dbName: String? = null

    override fun loadProperties(): Properties {
        val properties = loadProperties("test-config.properties")
        if (Common.hasContent(dbName)) {
            properties.setProperty(DBConfig.DB_URL, dbName)
        }
        return properties
    }
}