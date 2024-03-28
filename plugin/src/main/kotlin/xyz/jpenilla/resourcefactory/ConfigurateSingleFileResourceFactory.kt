package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.util.nullAction
import java.nio.file.Path
import java.util.function.Function
import javax.inject.Inject

/**
 * A factory that generates a single file using Configurate.
 */
abstract class ConfigurateSingleFileResourceFactory : SingleFileResourceFactory() {
    /**
     * The [ConfigurationLoader] factory to use.
     */
    @get:Nested
    abstract val loaderFactory: Property<Function<Path, out ConfigurationLoader<*>>>

    /**
     * Use a YAML loader.
     *
     * @param configure the configuration of the loader
     */
    fun yaml(configure: Action<YamlConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set(
            BuilderConfiguringLoaderFactory({ YamlConfigurationLoader.builder() }, configure)
        )
    }

    /**
     * Use a JSON loader.
     *
     * @param configure the configuration of the loader
     */
    fun json(configure: Action<GsonConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set(
            BuilderConfiguringLoaderFactory({ GsonConfigurationLoader.builder() }, configure)
        )
    }

    override fun generateSingleFile(outputFile: Path) {
        val loader = loaderFactory.get().apply(outputFile)
        loader.save(generateRootNode(loader))
    }

    /**
     * Generate the root node of the configuration.
     *
     * @param loader the loader to use
     * @return the root node
     */
    abstract fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N

    abstract class ObjectMapper @Inject constructor() : ConfigurateSingleFileResourceFactory() {
        @get:Nested
        abstract val value: Property<ValueProvider>

        fun value(value: Any) {
            this.value.set(ConstantValueProvider(value))
        }

        override fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N {
            val node = loader.createNode()
            node.set(value.get().asConfigSerializable())
            return node
        }

        fun interface ValueProvider {
            fun asConfigSerializable(): Any
        }

        data class ConstantValueProvider(
            @get:Nested
            val value: Any
        ) : ValueProvider {
            override fun asConfigSerializable(): Any = value
        }

        override fun toString(): String {
            return ObjectMapper::class.java.name + "(path=${path.orNull}, value=${value.orNull})"
        }
    }

    private class BuilderConfiguringLoaderFactory<L : AbstractConfigurationLoader<*>, B : AbstractConfigurationLoader.Builder<B, L>>(
        @get:Nested
        val builderFactory: () -> B,
        @get:Nested
        val configure: Action<B>
    ) : Function<Path, L> {
        override fun apply(path: Path): L = builderFactory()
            .also { configure.execute(it) }
            .path(path)
            .build()
    }
}
