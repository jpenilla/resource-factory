/*
 * Resource Factory Gradle Plugin
 * Copyright (c) 2024 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.resourcefactory

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.newInstance
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml
import xyz.jpenilla.resourcefactory.bungeecord.BungeeCordPluginYaml
import xyz.jpenilla.resourcefactory.bungeecord.bungeePluginYaml
import xyz.jpenilla.resourcefactory.fabric.FabricModJson
import xyz.jpenilla.resourcefactory.fabric.fabricModJson
import xyz.jpenilla.resourcefactory.neoforge.NeoForgeModsToml
import xyz.jpenilla.resourcefactory.neoforge.neoForgeModsToml
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml
import xyz.jpenilla.resourcefactory.paper.paperPluginYaml
import xyz.jpenilla.resourcefactory.velocity.VelocityPluginJson
import xyz.jpenilla.resourcefactory.velocity.velocityPluginJson
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * [SourceSet] extension for configuring [ResourceFactory]s.
 */
abstract class ResourceFactoryExtension @Inject constructor(
    private val objects: ObjectFactory,
    private val project: Project
) {
    /**
     * The [ResourceFactory]s for the default [ExecuteResourceFactories] task of the source set.
     */
    abstract val factories: ListProperty<ResourceFactory>

    /**
     * Add a [ResourceFactory] to the source set.
     *
     * @param factory the [ResourceFactory] to add
     */
    fun factory(factory: ResourceFactory) {
        factories.add(factory)
    }

    /**
     * Add multiple [ResourceFactory]s to the source set.
     *
     * @param factories the [ResourceFactory]s to add
     */
    fun factories(vararg factories: ResourceFactory) {
        this.factories.addAll(*factories)
    }

    /**
     * Create a [ResourceFactory] of type [T] with the given [constructionParams],
     * configure it with the given [configure] block, and add it to the source set.
     *
     * @param T the type of [ResourceFactory] to create
     * @param constructionParams the parameters to pass to the constructor of [T]
     * @param configure the block to configure the [ResourceFactory] with
     * @return the created and configured [ResourceFactory]
     */
    inline fun <reified T : ResourceFactory> factory(
        vararg constructionParams: Any,
        configure: Action<T>
    ): T = factory(T::class, constructionParams = constructionParams, configure = configure)

    /**
     * Create a [ResourceFactory] of type [T] with the given [constructionParams],
     * configure it with the given [configure] block, and add it to the source set.
     *
     * @param T the type of [ResourceFactory] to create
     * @param generatorType the type of [ResourceFactory] to create
     * @param constructionParams the parameters to pass to the constructor of [T]
     * @param configure the block to configure the [ResourceFactory] with
     * @return the created and configured [ResourceFactory]
     */
    fun <T : ResourceFactory> factory(
        generatorType: KClass<T>,
        vararg constructionParams: Any,
        configure: Action<T>
    ): T {
        val o = objects.newInstance(generatorType, *constructionParams)
        configure.execute(o)
        factory(o)
        return o
    }

    /**
     * Create a [PaperPluginYaml] and add it to the source set, configured with the given [configure] block.
     *
     * The created [PaperPluginYaml] will inherit the project's name, version, and description.
     *
     * @param configure the block to configure the [PaperPluginYaml] with
     * @return the created and configured [PaperPluginYaml]
     */
    fun paperPluginYaml(configure: Action<PaperPluginYaml>): PaperPluginYaml {
        val config = project.paperPluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }

    /**
     * Create a [BukkitPluginYaml] and add it to the source set, configured with the given [configure] block.
     *
     * The created [BukkitPluginYaml] will inherit the project's name, version, and description.
     *
     * @param configure the block to configure the [BukkitPluginYaml] with
     * @return the created and configured [BukkitPluginYaml]
     */
    fun bukkitPluginYaml(configure: Action<BukkitPluginYaml>): BukkitPluginYaml {
        val config = project.bukkitPluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }

    /**
     * Create a [VelocityPluginJson] and add it to the source set, configured with the given [configure] block.
     *
     * The created [VelocityPluginJson] will inherit the project's name (as [VelocityPluginJson.id] and [VelocityPluginJson.name]), version, and description.
     *
     * @param configure the block to configure the [VelocityPluginJson] with
     * @return the created and configured [VelocityPluginJson]
     */
    fun velocityPluginJson(configure: Action<VelocityPluginJson>): VelocityPluginJson {
        val config = project.velocityPluginJson(configure)
        factory(config.resourceFactory())
        return config
    }

    /**
     * Create a [FabricModJson] and add it to the source set, configured with the given [configure] block.
     *
     * The created [FabricModJson] will inherit the project's name (as [FabricModJson.id] and [FabricModJson.name]), version, and description.
     *
     * @param configure the block to configure the [FabricModJson] with
     * @return the created and configured [FabricModJson]
     */
    fun fabricModJson(configure: Action<FabricModJson>): FabricModJson {
        val config = project.fabricModJson(configure)
        factory(config.resourceFactory())
        return config
    }

    /**
     * Create a [NeoForgeModsToml] and add it to the source set, configured with the given [configure] block.
     *
     * @param configure the block to configure the [NeoForgeModsToml] with
     * @return the created and configured [NeoForgeModsToml]
     */
    fun neoForgeModsToml(configure: Action<NeoForgeModsToml>): NeoForgeModsToml {
        val config = project.neoForgeModsToml(configure)
        factory(config.resourceFactory())
        return config
    }

    /**
     * Create a [BungeeCordPluginYaml] and add it to the source set, configured with the given [configure] block.
     *
     * The created [BungeeCordPluginYaml] will inherit the project's name, version, and description.
     *
     * @param configure the block to configure the [BungeeCordPluginYaml] with
     * @return the created and configured [BungeeCordPluginYaml]
     */
    fun bungeePluginYaml(configure: Action<BungeeCordPluginYaml>): BungeeCordPluginYaml {
        val config = project.bungeePluginYaml(configure)
        factory(config.resourceFactory())
        return config
    }
}
