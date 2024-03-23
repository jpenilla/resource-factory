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
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty

fun Project.bungeePluginYml(configure: Action<BungeePluginYml> = nullAction()): BungeePluginYml {
    val yml = BungeePluginYml(objects)
    yml.setConventionsFromProjectMeta(this)
    configure.execute(yml)
    return yml
}

class BungeePluginYml constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider {

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
