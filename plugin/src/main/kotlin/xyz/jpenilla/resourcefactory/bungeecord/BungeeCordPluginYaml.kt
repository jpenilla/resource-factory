package xyz.jpenilla.resourcefactory.bungeecord

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
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.getValidating
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.validateAll

fun Project.bungeePluginYaml(configure: Action<BungeeCordPluginYaml> = nullAction()): BungeeCordPluginYaml {
    val yaml = BungeeCordPluginYaml(objects)
    yaml.setConventionsFromProjectMeta(this)
    configure.execute(yaml)
    return yaml
}

class BungeeCordPluginYaml constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider {

    companion object {
        private const val PLUGIN_NAME_PATTERN: String = "^[A-Za-z0-9_\\.-]+$"
        private const val FILE_NAME: String = "bungee.yml"
    }

    @Pattern(PLUGIN_NAME_PATTERN, "BungeeCord plugin name")
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

    override fun setConventionsFromProjectMeta(project: Project) {
        name.convention(project.name)
        description.convention(project.description)
        version.convention(project.version as String?)
    }

    override fun resourceFactory(): ResourceFactory {
        val factory = objects.newInstance(ConfigurateSingleFileResourceFactory.ObjectMapper::class)
        factory.yaml { nodeStyle(NodeStyle.BLOCK) }
        factory.path.set(FILE_NAME)
        factory.value.set(this)
        return factory
    }

    @ConfigSerializable
    class Serializable(yaml: BungeeCordPluginYaml) {
        val name = yaml::name.getValidating()
        val main = yaml.main.get()
        val version = yaml.version.orNull
        val author = yaml.author.orNull
        val depends = yaml.depends.nullIfEmpty()?.validateAll(PLUGIN_NAME_PATTERN, "BungeeCord plugin name (of dependency)")
        val softDepends = yaml.softDepends.nullIfEmpty()?.validateAll(PLUGIN_NAME_PATTERN, "BungeeCord plugin name (of soft dependency)")
        val description = yaml.description.orNull
    }
}
