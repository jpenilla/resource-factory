package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.util.nullAction
import java.nio.file.Path
import java.util.function.Function
import javax.inject.Inject

abstract class ConfigurateSingleFileResourceFactory : SingleFileResourceFactory() {
    @get:Nested
    abstract val loaderFactory: Property<Function<Path, ConfigurationLoader<*>>>

    // These helper methods look overcomplicated at first, but this is necessary to
    // have the value/configuration (implementation and nested properties) provided by the user count as task inputs.

    fun yaml(configure: Action<YamlConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set(object : Function<Path, ConfigurationLoader<*>> {
            @get:Nested
            val configure: Action<YamlConfigurationLoader.Builder> = configure

            override fun apply(path: Path): ConfigurationLoader<*> = YamlConfigurationLoader.builder()
                .also { this.configure.execute(it) }
                .path(path)
                .build()
        })
    }

    fun json(configure: Action<GsonConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set(object : Function<Path, ConfigurationLoader<*>> {
            @get:Nested
            val configure: Action<GsonConfigurationLoader.Builder> = configure

            override fun apply(path: Path): ConfigurationLoader<*> = GsonConfigurationLoader.builder()
                .also { this.configure.execute(it) }
                .path(path)
                .build()
        })
    }

    override fun generateSingleFile(outputFile: Path) {
        val loader = loaderFactory.get().apply(outputFile)
        loader.save(generateRootNode(loader))
    }

    abstract fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N

    abstract class ObjectMapper @Inject constructor() : ConfigurateSingleFileResourceFactory() {
        @get:Nested
        abstract val value: Property<ValueProvider>

        fun value(value: Any) {
            this.value.set(object : ValueProvider {
                @get:Nested
                val value = value

                override fun asConfigSerializable(): Any {
                    return this.value
                }
            })
        }

        override fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N {
            val node = loader.createNode()
            node.set(value.get().asConfigSerializable())
            return node
        }

        fun interface ValueProvider {
            fun asConfigSerializable(): Any
        }
    }
}
