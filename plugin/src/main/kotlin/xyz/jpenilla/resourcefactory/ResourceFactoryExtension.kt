package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml
import xyz.jpenilla.resourcefactory.bungeecord.BungeeCordPluginYaml
import xyz.jpenilla.resourcefactory.bungeecord.bungeePluginYaml
import xyz.jpenilla.resourcefactory.fabric.FabricModJson
import xyz.jpenilla.resourcefactory.fabric.fabricModJson
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml
import xyz.jpenilla.resourcefactory.paper.paperPluginYaml
import xyz.jpenilla.resourcefactory.velocity.VelocityPluginJson
import xyz.jpenilla.resourcefactory.velocity.velocityPluginJson
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class ResourceFactoryExtension @Inject constructor(
    private val objects: ObjectFactory,
    private val project: Project
) {
    abstract val factories: ListProperty<ResourceFactory>

    fun paperPluginYaml(configure: Action<PaperPluginYaml>): PaperPluginYaml {
        val config = project.paperPluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }

    fun bukkitPluginYaml(configure: Action<BukkitPluginYaml>): BukkitPluginYaml {
        val config = project.bukkitPluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }

    fun velocityPluginJson(configure: Action<VelocityPluginJson>): VelocityPluginJson {
        val config = project.velocityPluginJson(configure)
        factory(config.resourceFactory())
        return config
    }

    fun fabricModJson(configure: Action<FabricModJson>): FabricModJson {
        val config = project.fabricModJson(configure)
        factory(config.resourceFactory())
        return config
    }

    fun bungeePluginYaml(configure: Action<BungeeCordPluginYaml>): BungeeCordPluginYaml {
        val config = project.bungeePluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }

    inline fun <reified T : ResourceFactory> factory(
        vararg params: Any,
        configure: Action<T>
    ): T = factory(T::class, params = params, configure = configure)

    fun <T : ResourceFactory> factory(
        generatorType: KClass<T>,
        vararg params: Any,
        configure: Action<T>
    ): T {
        val o = objects.newInstance(generatorType, *params)
        configure.execute(o)
        factory(o)
        return o
    }

    fun factory(generator: ResourceFactory) {
        factories.add(generator)
    }

    fun factories(vararg generator: ResourceFactory) {
        factories.addAll(*generator)
    }
}
