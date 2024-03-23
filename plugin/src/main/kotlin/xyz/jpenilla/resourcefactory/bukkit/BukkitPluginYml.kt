package xyz.jpenilla.resourcefactory.bukkit

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import java.nio.file.Path

fun Project.bukkitPluginYml(configure: Action<BukkitPluginYml> = nullAction()): BukkitPluginYml {
    val yml = BukkitPluginYml(objects)
    yml.copyProjectMeta(this)
    configure.execute(yml)
    return yml
}

class BukkitPluginYml(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider {

    @get:Input
    @get:Optional
    val apiVersion: Property<String> = objects.property()

    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val version: Property<String> = objects.property()

    @get:Input
    val main: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val description: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val load: Property<PluginLoadOrder> = objects.property()

    @get:Input
    @get:Optional
    val author: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val authors: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val website: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val depend: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val softDepend: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val loadBefore: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val prefix: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val defaultPermission: Property<Permission.Default> = objects.property()

    @get:Input
    @get:Optional
    val provides: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val libraries: ListProperty<String> = objects.listProperty()

    @get:Nested
    val commands: NamedDomainObjectContainer<Command> = objects.domainObjectContainer(Command::class.java) { Command(it) }

    @get:Nested
    val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class.java) { Permission(it) }

    @get:Input
    @get:Optional
    val foliaSupported: Property<Boolean> = objects.property()

    enum class PluginLoadOrder {
        STARTUP,
        POSTWORLD
    }

    /**
     * Copy the name, version, and description from the provided project.
     *
     * [project] project
     */
    fun copyProjectMeta(project: Project) {
        name.convention(project.name)
        version.convention(project.version as String?)
        description.convention(project.description)
    }

    @ConfigSerializable
    data class Command(
        @Transient
        @Input val name: String
    ) {
        @Input
        @Optional
        var description: String? = null

        @Input
        @Optional
        var aliases: List<String>? = null

        @Input
        @Optional
        var permission: String? = null

        @Input
        @Optional
        var permissionMessage: String? = null

        @Input
        @Optional
        var usage: String? = null
    }

    fun resourceFactory(): ResourceFactory {
        val gen = objects.newInstance(
            ConfigurateSingleFileResourceFactory.ObjectMapper::class,
            { path: Path ->
                YamlConfigurationLoader.builder()
                    .defaultOptions {
                        it.serializers { s ->
                            s.registerExact(Permission.Default::class.java, Permission.Default.Serializer)
                        }
                    }
                    .path(path)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build()
            }
        )
        gen.path.set("plugin.yml")
        gen.value.set(this)
        return gen
    }

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    @ConfigSerializable
    class Serializable(yml: BukkitPluginYml) {
        val apiVersion = yml.apiVersion.orNull
        val name = yml.name.get()
        val version = yml.version.get()
        val main = yml.main.get()
        val description = yml.description.orNull
        val load = yml.load.orNull
        val author = yml.author.orNull
        val authors = yml.authors.nullIfEmpty()
        val website = yml.website.orNull
        val depend = yml.depend.nullIfEmpty()
        val softDepend = yml.softDepend.nullIfEmpty()
        val loadBefore = yml.loadBefore.nullIfEmpty()
        val prefix = yml.prefix.orNull
        val defaultPermission = yml.defaultPermission.orNull
        val provides = yml.provides.nullIfEmpty()
        val libraries = yml.libraries.nullIfEmpty()
        val commands = yml.commands.nullIfEmpty()
        val permissions = yml.permissions.nullIfEmpty()
        val foliaSupported = yml.foliaSupported.orNull
    }
}
