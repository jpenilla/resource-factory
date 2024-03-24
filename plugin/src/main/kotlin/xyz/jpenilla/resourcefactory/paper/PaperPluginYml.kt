package xyz.jpenilla.resourcefactory.paper

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.NodeStyle
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.bukkit.Permission
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.getValidating
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.orNullValidating
import xyz.jpenilla.resourcefactory.util.validateAll
import javax.inject.Inject

fun Project.paperPluginYml(configure: Action<PaperPluginYml> = nullAction()): PaperPluginYml {
    val yml = PaperPluginYml(objects)
    yml.setConventionsFromProjectMeta(this)
    configure.execute(yml)
    return yml
}

class PaperPluginYml constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider {
    companion object {
        private const val PLUGIN_NAME_PATTERN: String = "^[A-Za-z0-9_\\.-]+$"
        private const val PLUGIN_CLASS_PATTERN: String = "^(?!io\\.papermc\\.)([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$"
    }

    @get:Input
    val apiVersion: Property<String> = objects.property()

    @Pattern(PLUGIN_NAME_PATTERN, "Paper plugin name")
    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val version: Property<String> = objects.property()

    @Pattern(PLUGIN_CLASS_PATTERN, "Paper plugin main class name")
    @get:Input
    val main: Property<String> = objects.property()

    @Pattern(PLUGIN_CLASS_PATTERN, "Paper plugin loader class name")
    @get:Input
    @get:Optional
    val loader: Property<String> = objects.property()

    @Pattern(PLUGIN_CLASS_PATTERN, "Paper plugin bootstrapper class name")
    @get:Input
    @get:Optional
    val bootstrapper: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val description: Property<String> = objects.property()

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
    val prefix: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val defaultPermission: Property<Permission.Default> = objects.property()

    @get:Input
    @get:Optional
    val foliaSupported: Property<Boolean> = objects.property()

    @get:Nested
    var dependencies: Dependencies = objects.newInstance(Dependencies::class)

    @get:Nested
    val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class) { Permission(objects, it) }

    fun dependencies(configure: Action<Dependencies>) {
        configure.execute(dependencies)
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

    enum class Load {
        BEFORE,
        AFTER,
        OMIT
    }

    abstract class Dependencies @Inject constructor(objects: ObjectFactory) {
        @get:Nested
        val bootstrap: NamedDomainObjectContainer<Dependency> = objects.domainObjectContainer(Dependency::class) { Dependency(objects, it) }

        @get:Nested
        val server: NamedDomainObjectContainer<Dependency> = objects.domainObjectContainer(Dependency::class) { Dependency(objects, it) }

        fun bootstrap(
            name: String,
            load: Load = Load.OMIT,
            required: Boolean = true,
            joinClasspath: Boolean = true
        ): NamedDomainObjectProvider<Dependency> = bootstrap.register(name) {
            this.load.set(load)
            this.required.set(required)
            this.joinClasspath.set(joinClasspath)
        }

        fun server(
            name: String,
            load: Load = Load.OMIT,
            required: Boolean = true,
            joinClasspath: Boolean = true
        ): NamedDomainObjectProvider<Dependency> = server.register(name) {
            this.load.set(load)
            this.required.set(required)
            this.joinClasspath.set(joinClasspath)
        }
    }

    class Dependency(
        objects: ObjectFactory,
        @get:Input
        val name: String
    ) {
        @get:Input
        val load: Property<Load> = objects.property<Load>().convention(Load.OMIT)

        @get:Input
        val required: Property<Boolean> = objects.property<Boolean>().convention(true)

        @get:Input
        val joinClasspath: Property<Boolean> = objects.property<Boolean>().convention(true)
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
        gen.path.set("paper-plugin.yml")
        gen.value.set(this)
        return gen
    }

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    @ConfigSerializable
    class Serializable(yml: PaperPluginYml) {
        val apiVersion = yml.apiVersion.get()
        val name = yml::name.getValidating()
        val version = yml.version.get()
        val main = yml::main.getValidating()
        val loader = yml::loader.orNullValidating()
        val bootstrapper = yml::bootstrapper.orNullValidating()
        val description = yml.description.orNull
        val author = yml.author.orNull
        val authors = yml.authors.nullIfEmpty()
        val website = yml.website.orNull
        val prefix = yml.prefix.orNull
        val defaultPermission = yml.defaultPermission.orNull
        val foliaSupported = yml.foliaSupported.orNull
        val dependencies = SerializableDependencies.from(yml.dependencies)
        val permissions = yml.permissions.nullIfEmpty()?.mapValues { Permission.Serializable(it.value) }
    }

    @ConfigSerializable
    data class SerializableDependency(val load: Load, val required: Boolean, val joinClasspath: Boolean) {
        companion object {
            fun from(dep: Dependency) = SerializableDependency(dep.load.get(), dep.required.get(), dep.joinClasspath.get())
        }
    }

    @ConfigSerializable
    data class SerializableDependencies(
        val bootstrap: Map<String, SerializableDependency>?,
        val server: Map<String, SerializableDependency>?
    ) {
        companion object {
            fun from(deps: Dependencies): SerializableDependencies? {
                val bs = deps.bootstrap.nullIfEmpty()?.mapValues { SerializableDependency.from(it.value) }
                    .also { it?.keys?.validateAll(PLUGIN_NAME_PATTERN, "Paper plugin name (of bootstrap dependency)") }
                val server = deps.server.nullIfEmpty()?.mapValues { SerializableDependency.from(it.value) }
                    .also { it?.keys?.validateAll(PLUGIN_NAME_PATTERN, "Paper plugin name (of server dependency)") }
                if (bs == null && server == null) {
                    return null
                }
                return SerializableDependencies(bs, server)
            }
        }
    }
}
