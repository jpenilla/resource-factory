package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
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
        val configure: Action<B>
    ) : Function<Path, L> {
        override fun apply(path: Path): L = builderFactory()
            .also { configure.execute(it) }
            .path(path)
            .build()
    }
}
