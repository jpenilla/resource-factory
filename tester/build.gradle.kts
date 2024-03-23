import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
    java
    id("xyz.jpenilla.resource-factory-paper-convention")
    id("xyz.jpenilla.resource-factory-bukkit-convention")
    id("xyz.jpenilla.resource-factory-bungee-convention")
    id("xyz.jpenilla.resource-factory-velocity-convention")
    id("xyz.jpenilla.resource-factory-fabric-convention")
}

version = "0.0.1-test"
description = "Resource Factory tester"

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

sourceSets.main {
    resourceFactory {
        factory(ConfigurateSingleFileResourceFactory.ObjectMapper::class) {
            yaml()
            path = "custom-data-dir/custom-data.yml"
            value(CustomData("Steve", 123))
        }
    }
}

@ConfigSerializable
class CustomData(
    @get:Input
    val name: String,
    @get:Input
    val number: Int
)
