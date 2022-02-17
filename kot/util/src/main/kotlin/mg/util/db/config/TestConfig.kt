package mg.util.db.config

import mg.util.common.Common
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
