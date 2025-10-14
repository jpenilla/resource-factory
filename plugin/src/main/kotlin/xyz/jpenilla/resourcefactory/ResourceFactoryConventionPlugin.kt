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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions

abstract class ResourceFactoryConventionPlugin<E : Any>(
    private val extensionName: String,
    private val extensionFactory: (Project) -> E,
    private val sourceSetName: String = "main",
    private val configureFactoryExt: (ResourceFactoryExtension, E) -> Unit
) : Plugin<Project> {
    abstract class Provider<E : ResourceFactory.Provider>(
        extensionName: String,
        extensionFactory: (Project) -> E,
        sourceSetName: String = "main",
    ) : ResourceFactoryConventionPlugin<E>(
        extensionName,
        extensionFactory,
        sourceSetName,
        { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
    )

    override fun apply(target: Project) {
        target.plugins.apply(ResourceFactoryPlugin::class)
        target.plugins.withType(JavaBasePlugin::class) {
            val ext = extensionFactory(target)
            touchExtension(target, ext)
            target.extensions.add(extensionName, ext)
            target.extensions.getByType(SourceSetContainer::class).named(sourceSetName) {
                extensions.configure(ResourceFactoryExtension::class) {
                    configureFactoryExt(this, ext)
                }
            }
        }
    }

    open fun touchExtension(target: Project, ext: E) {
        if (ext is ProjectMetaConventions) {
            // we need to set the conventions again, since the object was created eagerly.
            target.afterEvaluate { ext.setConventionsFromProjectMeta(this) }
        }
    }
}
