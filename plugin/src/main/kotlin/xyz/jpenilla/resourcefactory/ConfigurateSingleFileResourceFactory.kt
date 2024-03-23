package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.util.nullAction
import java.nio.file.Path
import javax.inject.Inject

abstract class ConfigurateSingleFileResourceFactory : SingleFileResourceFactory() {
    @get:Internal
    abstract val loaderFactory: Property<(Path) -> ConfigurationLoader<*>>

    fun yaml(configure: Action<YamlConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set { path -> YamlConfigurationLoader.builder().also { configure.execute(it) }.path(path).build() }
    }

    fun json(configure: Action<GsonConfigurationLoader.Builder> = nullAction()) {
        loaderFactory.set { path -> GsonConfigurationLoader.builder().also { configure.execute(it) }.path(path).build() }
    }

    override fun generateSingleFile(outputFile: Path) {
        val loader = loaderFactory.get()(outputFile)
        loader.save(generateRootNode(loader))
    }

    abstract fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N

    abstract class ObjectMapper @Inject constructor() : ConfigurateSingleFileResourceFactory() {
        @get:Nested
        abstract val value: Property<ValueProvider>

        fun value(value: Any) {
            this.value.set { value }
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
