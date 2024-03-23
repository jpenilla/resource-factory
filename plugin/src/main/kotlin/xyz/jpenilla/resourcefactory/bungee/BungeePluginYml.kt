package xyz.jpenilla.resourcefactory.bungee

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import java.nio.file.Path

fun Project.bungeePluginYml(configure: Action<BungeePluginYml> = nullAction()): BungeePluginYml {
    val yml = BungeePluginYml(objects)
    yml.copyProjectMeta(this)
    configure.execute(yml)
    return yml
}

class BungeePluginYml constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider {

    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val main: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val version: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val description: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val author: Property<String> = objects.property()

    @get:Input
    val depends: SetProperty<String> = objects.setProperty()

    @get:Input
    val softDepends: SetProperty<String> = objects.setProperty()

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    fun copyProjectMeta(project: Project) {
        name.convention(project.name)
        description.convention(project.description)
        version.convention(project.version as String?)
    }

    fun resourceFactory(): ResourceFactory {
        val factory = objects.newInstance(
            ConfigurateSingleFileResourceFactory.ObjectMapper::class,
            { path: Path ->
                YamlConfigurationLoader.builder()
                    .path(path)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build()
            }
        )
        factory.path.set("bungee.yml")
        factory.value.set(this)
        return factory
    }

    @ConfigSerializable
    class Serializable(yml: BungeePluginYml) {
        val name = yml.name.get()
        val main = yml.main.get()
        val version = yml.version.orNull
        val author = yml.author.orNull
        val depends = yml.depends.nullIfEmpty()
        val softDepends = yml.softDepends.nullIfEmpty()
        val description = yml.description.orNull
    }
}
