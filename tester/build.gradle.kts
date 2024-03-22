plugins {
    java
    id("xyz.jpenilla.resource-factory")
}

version = "0.0.1-test"
description = "Resource Factory tester"

sourceSets.main {
    resourceFactory {
        paperPluginYml {
            main = "test"
            apiVersion = "1.20"
        }
        bukkitPluginYml {
            main = "test"
        }
    }
}
