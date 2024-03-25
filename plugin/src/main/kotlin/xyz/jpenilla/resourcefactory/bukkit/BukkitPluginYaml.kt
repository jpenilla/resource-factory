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
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.getValidating
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.validateAll

fun Project.bukkitPluginYaml(configure: Action<BukkitPluginYaml> = nullAction()): BukkitPluginYaml {
    val yaml = BukkitPluginYaml(objects)
    yaml.setConventionsFromProjectMeta(this)
    configure.execute(yaml)
    return yaml
}

class BukkitPluginYaml(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider {

    companion object {
        private const val PLUGIN_NAME_PATTERN: String = "^[A-Za-z0-9_\\.-]+$"
        private const val FILE_NAME: String = "plugin.yml"
    }

    @get:Input
    @get:Optional
    val apiVersion: Property<String> = objects.property()

    @Pattern(PLUGIN_NAME_PATTERN, "Bukkit plugin name")
    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val version: Property<String> = objects.property()

    @Pattern("^(?!org\\.bukkit\\.)([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$", "Bukkit plugin main class name")
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
    val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class.java) { Permission(objects, it) }

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
    override fun setConventionsFromProjectMeta(project: Project) {
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

    override fun resourceFactory(): ResourceFactory {
        val gen = objects.newInstance(ConfigurateSingleFileResourceFactory.ObjectMapper::class)
        gen.yaml {
            defaultOptions {
                it.serializers { s ->
                    s.registerExact(Permission.Default::class.java, Permission.Default.Serializer)
                }
            }.nodeStyle(NodeStyle.BLOCK)
        }
        gen.path.set(FILE_NAME)
        gen.value.set(this)
        return gen
    }

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    @ConfigSerializable
    class Serializable(yaml: BukkitPluginYaml) {
        val apiVersion = yaml.apiVersion.orNull
        val name = yaml::name.getValidating()
        val version = yaml.version.get()
        val main = yaml::main.getValidating()
        val description = yaml.description.orNull
        val load = yaml.load.orNull
        val author = yaml.author.orNull
        val authors = yaml.authors.nullIfEmpty()
        val website = yaml.website.orNull
        val depend = yaml.depend.nullIfEmpty()?.validateAll(PLUGIN_NAME_PATTERN, "Bukkit plugin name (of depend)")
        val softDepend = yaml.softDepend.nullIfEmpty()?.validateAll(PLUGIN_NAME_PATTERN, "Bukkit plugin name (of softDepend)")
        val loadBefore = yaml.loadBefore.nullIfEmpty()?.validateAll(PLUGIN_NAME_PATTERN, "Bukkit plugin name (of loadBefore)")
        val prefix = yaml.prefix.orNull
        val defaultPermission = yaml.defaultPermission.orNull
        val provides = yaml.provides.nullIfEmpty()
        val libraries = yaml.libraries.nullIfEmpty()
        val commands = yaml.commands.nullIfEmpty()
        val permissions = yaml.permissions.nullIfEmpty()?.mapValues { Permission.Serializable(it.value) }
        val foliaSupported = yaml.foliaSupported.orNull
    }
}
