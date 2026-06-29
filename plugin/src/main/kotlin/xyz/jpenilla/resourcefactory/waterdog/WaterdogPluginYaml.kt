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
package xyz.jpenilla.resourcefactory.waterdog

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
import xyz.jpenilla.resourcefactory.ResourceFactoryExtension
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.getValidating
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmptyValidating
import javax.inject.Inject

/**
 * Create a [WaterdogPluginYaml] and configure it with the given [configure] block.
 *
 * The created [WaterdogPluginYaml] will inherit the project's name and version.
 *
 * @param configure the block to configure the [WaterdogPluginYaml] with
 * @return the created and configured [WaterdogPluginYaml]
 */
fun Project.waterdogPluginYaml(configure: Action<WaterdogPluginYaml> = nullAction()): WaterdogPluginYaml {
    val yaml = objects.newInstance<WaterdogPluginYaml>()
    yaml.setConventionsFromProjectMeta(this)
    configure.execute(yaml)
    return yaml
}

/**
 * A WaterdogPE `waterdog.yml`/`plugin.yml` configuration.
 *
 * WaterdogPE loads the plugin configuration from a `waterdog.yml` file, falling back to `plugin.yml`
 * if the former is not present. This factory generates `waterdog.yml` by default.
 *
 * @see [waterdogPluginYaml]
 * @see [ResourceFactoryExtension.waterdogPluginYaml]
 */
abstract class WaterdogPluginYaml @Inject constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.Simple.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider {

    companion object {
        private const val PLUGIN_NAME_PATTERN: String = "^[A-Za-z0-9_\\.-]+$"
        private const val FILE_NAME: String = "waterdog.yml"
    }

    @Pattern(PLUGIN_NAME_PATTERN, "WaterdogPE plugin name")
    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val main: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val version: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val author: Property<String> = objects.property()

    @get:Input
    @Pattern(PLUGIN_NAME_PATTERN, "WaterdogPE plugin name (of dependency)")
    val depends: SetProperty<String> = objects.setProperty()

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    override fun setConventionsFromProjectMeta(project: Project) {
        name.convention(project.name)
        version.convention(project.version.toString())
    }

    override fun resourceFactory(): ResourceFactory {
        val factory = objects.newInstance(ConfigurateSingleFileResourceFactory.Simple::class)
        factory.yaml { nodeStyle(NodeStyle.BLOCK) }
        factory.path.set(FILE_NAME)
        factory.value.set(this)
        return factory
    }

    @ConfigSerializable
    class Serializable(yaml: WaterdogPluginYaml) {
        val name = yaml::name.getValidating()
        val main = yaml.main.get()
        val version = yaml.version.orNull
        val author = yaml.author.orNull
        val depends = yaml::depends.nullIfEmptyValidating()
    }
}
