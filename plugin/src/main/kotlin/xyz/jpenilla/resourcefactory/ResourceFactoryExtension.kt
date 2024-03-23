package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYml
import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYml
import xyz.jpenilla.resourcefactory.bungee.BungeePluginYml
import xyz.jpenilla.resourcefactory.bungee.bungeePluginYml
import xyz.jpenilla.resourcefactory.fabric.FabricModJson
import xyz.jpenilla.resourcefactory.fabric.fabricModJson
import xyz.jpenilla.resourcefactory.paper.PaperPluginYml
import xyz.jpenilla.resourcefactory.paper.paperPluginYml
import xyz.jpenilla.resourcefactory.velocity.VelocityPluginJson
import xyz.jpenilla.resourcefactory.velocity.velocityPluginJson
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class ResourceFactoryExtension @Inject constructor(
    private val objects: ObjectFactory,
    private val project: Project
) {
    abstract val factories: ListProperty<ResourceFactory>

    fun paperPluginYml(configure: Action<PaperPluginYml>): PaperPluginYml {
        val config = project.paperPluginYml(configure)
        factory(config.resourceFactory())
        return config
    }

    fun bukkitPluginYml(configure: Action<BukkitPluginYml>): BukkitPluginYml {
        val config = project.bukkitPluginYml(configure)
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

    fun bungeePluginYml(configure: Action<BungeePluginYml>): BungeePluginYml {
        val config = project.bungeePluginYml(configure)
        factory(config.resourceFactory())
        return config
    }

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
