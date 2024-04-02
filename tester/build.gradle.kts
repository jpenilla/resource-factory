import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.bukkit.Permission
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

paperPluginYaml {
    main = "test"
    apiVersion = "1.20"
    dependencies {
        server("squaremap")
    }
}

bukkitPluginYaml {
    main = "test"
    permissions {
        register("permission") {
            description = "A permission"
            default = Permission.Default.OP
            children("permission.a", "permission.b")
        }
        register("another_permission")
    }
    commands {
        register("test-command") {
            description = "A test command"
            aliases = listOf("test-command-alias")
            permission = "test-command-permission"
        }
    }
}

velocityPluginJson {
    main = "test"
    dependency("luckperms")
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

    custom("test_list", listOf("test1", "test2"))
    custom("test_map", mapOf("key" to "value"))
    custom("person", CustomData("Steve", 123))
    custom("person_list", listOf(CustomData("Steve", 123), CustomData("Bob", 456)))
    custom("person_map", mapOf("steve" to CustomData("Steve", 123), "bob" to CustomData("Bob", 456)))
}

bungeePluginYaml {
    main = "test"
}

sourceSets.main {
    resourceFactory {
        factory<ConfigurateSingleFileResourceFactory.Simple> {
            yaml()
            path = "custom-data-dir/custom-data.yaml"
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
