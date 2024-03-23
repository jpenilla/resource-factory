import xyz.jpenilla.resourcefactory.fabric.Environment

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
        velocityPluginJson {
            main = "test"
        }
        fabricModJson {
            environment = Environment.ANY
            clientEntrypoint("client.Entry")
            mixin("my-mixins.json") {
                environment = Environment.ANY
            }
            author("MyName") {
                contact.homepage = "https://linkedin.com/BobSmith"
            }
            contact {
                homepage = "https://github.com/Me/MyProject"
            }
            icon("icon.png")
            depends("some_other_mod", "*")
            apache2License()
        }
        bungeePluginYml {
            main = "test"
        }
    }
}
