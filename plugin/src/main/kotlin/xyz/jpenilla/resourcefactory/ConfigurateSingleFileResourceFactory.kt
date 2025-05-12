package xyz.jpenilla.resourcefactory

import io.github.wasabithumb.jtoml.configurate.TomlConfigurationLoader
import io.github.wasabithumb.jtoml.option.JTomlOption
import io.github.wasabithumb.jtoml.option.prop.LineSeparator
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.util.nullAction
import java.nio.file.Path
import javax.inject.Inject

/**
 * A factory that generates a single file using Configurate.
 */
abstract class ConfigurateSingleFileResourceFactory : SingleFileResourceFactory() {

    /**
     * Creates a [ConfigurationLoader] for any given path.
     */
    interface LoaderFactory {

        /**
         * Creates a [ConfigurationLoader] for the given path.
         *
         * @param path the path to create the loader for
         */
        fun createLoader(path: Path): ConfigurationLoader<*>
    }

    /**
     * The [LoaderFactory] to use.
     */
    @get:Nested
    abstract val loaderFactory: Property<LoaderFactory>

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

    /**
     * Use a TOML loader.
     *
     * @param configure the configuration of the loader
     */
    @ApiStatus.Experimental
    fun toml(configure: Action<TomlConfigurationLoader.Builder> = nullAction()) {
        val builderFactory = {
            TomlConfigurationLoader.builder()
                .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
                .set(JTomlOption.WRITE_EMPTY_TABLES, true)
        }
        loaderFactory.set(BuilderConfiguringLoaderFactory(builderFactory, configure))
    }

    override fun generateSingleFile(outputFile: Path) {
        val loader = loaderFactory.get().createLoader(outputFile)
        loader.save(generateRootNode(loader))
    }

    /**
     * Generate the root node of the configuration.
     *
     * @param loader the loader to use
     * @return the root node
     */
    abstract fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N

    /**
     * An extension of [ConfigurateSingleFileResourceFactory] for the simple case of serializing a single value.
     */
    abstract class Simple @Inject constructor() : ConfigurateSingleFileResourceFactory() {
        /**
         * Provides the value to serialize.
         *
         * @see ValueProvider
         */
        @get:Nested
        abstract val value: Property<ValueProvider>

        /**
         * Sets a constant value, where the object satisfies both Gradle and Configurate's expectations.
         *
         * @see ValueProvider
         * @see ConstantValueProvider
         */
        fun value(value: Any) {
            this.value.set(ConstantValueProvider(value))
        }

        override fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N {
            val node = loader.createNode()
            node.set(value.get().asConfigSerializable())
            return node
        }

        /**
         * Provides a serializable value. When using Gradle's [ObjectFactory] API, or lazy configuration system (i.e. [Property] APIs
         * and task input annotations), it's not uncommon to end up with types that are not easily passed to serialization frameworks.
         *
         * [ValueProvider] avoids this problem by separating the Gradle and serialization models. The [ValueProvider] implementation
         * satisfies the Gradle model, while the result of the [ValueProvider.asConfigSerializable] method satisfies the serialization model.
         */
        fun interface ValueProvider {
            /**
             * Returns the serializable value.
             *
             * @return the serializable value
             */
            fun asConfigSerializable(): Any
        }

        data class ConstantValueProvider(
            @get:Nested
            val value: Any
        ) : ValueProvider {
            override fun asConfigSerializable(): Any = value
        }

        override fun toString(): String {
            return Simple::class.java.name + "(path=${path.orNull}, value=${value.orNull})"
        }
    }

    private class BuilderConfiguringLoaderFactory<L : AbstractConfigurationLoader<*>, B : AbstractConfigurationLoader.Builder<B, L>>(
        @get:Nested
        val builderFactory: () -> B,
        @get:Nested
        val userConfigure: Action<B>,
    ) : LoaderFactory {
        override fun createLoader(path: Path): ConfigurationLoader<*> = builderFactory()
            .also { userConfigure.execute(it) }
            .path(path)
            .build()
    }
}
