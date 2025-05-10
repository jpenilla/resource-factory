package xyz.jpenilla.resourcefactory.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.wasabithumb.jtoml.JToml
import org.gradle.api.Action
import org.gradle.api.tasks.Nested
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AtomicFiles
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.reference.ConfigurationReference
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory.LoaderFactory
import java.io.BufferedWriter
import java.io.StringWriter
import java.nio.file.Path
import kotlin.io.path.createDirectories

@ApiStatus.Internal
class TomlConfigurationWriter(
    private val path: Path,
    userConfigure: Action<GsonConfigurationLoader.Builder>,
) : ConfigurationLoader<BasicConfigurationNode> {
    companion object {
        private val GSON = GsonBuilder().create()
    }

    private var writer: Pair<BufferedWriter, StringWriter>? = null
    private val gsonLoader = GsonConfigurationLoader.builder()
        .also { userConfigure.execute(it) }
        .sink { requireNotNull(writer).first }
        .build()

    override fun load(options: ConfigurationOptions?): BasicConfigurationNode? {
        throw UnsupportedOperationException("loading is not supported")
    }

    override fun loadToReference(): ConfigurationReference<BasicConfigurationNode?>? {
        throw UnsupportedOperationException("loading is not supported")
    }

    @Synchronized
    override fun save(node: ConfigurationNode?) {
        val stringWriter = StringWriter()
        val bufferedWriter = BufferedWriter(stringWriter)
        writer = bufferedWriter to stringWriter
        try {
            gsonLoader.save(node)
            write(requireNotNull(writer).second)
        } finally {
            writer = null
        }
    }

    private fun write(stringWriter: StringWriter) {
        val buffer = stringWriter.toString()
        val jsonObject = GSON.fromJson(buffer, JsonObject::class.java)
        val jtoml = JToml.jToml()
        val tomlTable = jtoml.deserialize(JsonObject::class.java, jsonObject)
        val serialized = jtoml.writeToString(tomlTable)

        path.parent.createDirectories()
        AtomicFiles.atomicBufferedWriter(path, Charsets.UTF_8).use { writer ->
            writer.write(serialized)
        }
    }

    override fun defaultOptions(): ConfigurationOptions? = gsonLoader.defaultOptions()

    override fun createNode(options: ConfigurationOptions?): BasicConfigurationNode? = gsonLoader.createNode(options)

    class Factory(
        @get:Nested
        val userConfigure: Action<GsonConfigurationLoader.Builder>,
    ) : LoaderFactory {
        override fun createLoader(path: Path): ConfigurationLoader<*> = TomlConfigurationWriter(path, userConfigure)
    }
}
