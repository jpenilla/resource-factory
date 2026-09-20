import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.Permission
import xyz.jpenilla.resourcefactory.fabric.Environment
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

plugins {
    java
    id("xyz.jpenilla.resource-factory-paper-convention")
    id("xyz.jpenilla.resource-factory-bukkit-convention")
    id("xyz.jpenilla.resource-factory-bungee-convention")
    id("xyz.jpenilla.resource-factory-velocity-convention")
    id("xyz.jpenilla.resource-factory-fabric-convention")
    id("xyz.jpenilla.resource-factory-neoforge-convention")
}

version = "0.0.1-test"
description = "Resource Factory tester"

paperPluginYaml {
    main = "test"
    apiVersion = "1.20"
    load = BukkitPluginYaml.PluginLoadOrder.POSTWORLD
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

    custom("test_list", simpleCustomValueList(listOf("test1", "test2")))
    custom("test_map", simpleCustomValueMap(mapOf("key" to "value")))
    custom("person", complexCustomValue(CustomData("Steve", 123)))
    custom(
        "person_list",
        typedComplexCustomValue(
            typeOf<List<CustomData>>().javaType,
            listOf(CustomData("Steve", 123), CustomData("Bob", 456))
        )
    )
    custom(
        "person_map",
        typedComplexCustomValue(
            typeOf<Map<String, CustomData>>().javaType,
            mapOf("steve" to CustomData("Steve", 123), "bob" to CustomData("Bob", 456))
        )
    )
}

neoForgeModsToml {
    loaderVersion = "*"
    apache2License()
    showAsResourcePack = true
    showAsDataPack = true
    services.add("example.TestService")
    issueTrackerUrl = "https://example.com/issues"
    logoFile = "default-legacy-logo.png"
    logoBlur = false
    bannerFile = "default-banner.png"
    iconFile = "icon.png"
    iconBlur = false
    conventionMod {
        namespace = "tester_namespace"
        logoFile = "legacy-logo.png"
        logoBlur = true
        bannerFile = "banner.png"
        iconFile = "tester-icon.png"
        iconBlur = true
        updateJsonUrl = "https://example.com/updates.json"
        modUrl = "https://example.com/mod"
        displayUrl = "https://example.com/display"
        credits = "Test contributors"
        authors = "Test author"
        enumExtensions = "META-INF/enumextensions.json"
        featureFlags = "META-INF/feature_flags.json"
        features.put("javaVersion", "[25,)")
        modProperties.put("test_property", simpleCustomValue("test_value"))
        dependencies {
            required("minecraft", "1.21.5") {
                reason = "Minecraft"
                after()
                client()
                referralUrl = "https://www.minecraft.net"
            }
            optional("moonrise")
            optional("example.dotted_mod")
            incompatible("incompatible_mod", "[1,2)") {
                before()
                server()
            }
            discouraged("discouraged_mod")
        }
        custom.put(
            "ferritecore:disabled_options",
            simpleCustomValueList(
                listOf(
                    "replaceNeighborLookup",
                    "replacePropertyMap",
                )
            )
        )
    }
    mod("my_mod_id") {
        setConventionsFromProjectMeta(project)
    }
    mod("example.dotted_mod") {
        setConventionsFromProjectMeta(project)
        features.put("javaVersion", "[25,)")
        modProperties.put("enabled", simpleCustomValue(false))
        modProperties.put("count", simpleCustomValue(42))
        modProperties.put("labels", simpleCustomValueList(listOf("first", "second")))
        modProperties.put("options", simpleCustomValueMap(mapOf("literal.dotted.key" to true)))
        modProperties.put("person", complexCustomValue(CustomData("Steve", 123)))
        dependencies {
            required("tester")
        }
    }
    mods.register("my_other_mod_id")
    accessTransformers("a.cfg", "b.cfg")
    mixin("tester.mixins.json") {
        requiredMods.add("moonrise")
        behaviorVersion = "0.17.1"
    }
    mixin("unconditional.mixins.json")
    custom.put(
        "lithium:options",
        simpleCustomValueMap(
            mapOf(
                "mixin.ai.poi" to false,
                "mixin.alloc.deep_passengers" to false,
            )
        )
    )
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
