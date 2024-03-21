plugins {
    java
    id("xyz.jpenilla.resource-factory")
}

version = "0.0.1-test"

sourceSets.main {
    resourceFactory {
        paperPluginYml {
            main = "test"
        }
        bukkitPluginYml {
            main = "test"
        }
    }
}
