package xyz.jpenilla.resourcefactory

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

abstract class ResourceFactoryConventionPlugin<E : Any>(
    private val extensionName: String,
    private val extensionFactory: (Project) -> E,
    private val sourceSetName: String,
    private val configureSourceSetFactories: (ResourceFactoryExtension, E) -> Unit
) : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(ResourceFactoryPlugin::class)
        target.plugins.withType(JavaBasePlugin::class) {
            val ext = extensionFactory(target)
            touchExtension(target, ext)
            target.extensions.add(extensionName, ext)
            target.extensions.getByType(SourceSetContainer::class).named(sourceSetName) {
                extensions.configure(ResourceFactoryExtension::class) {
                    configureSourceSetFactories(this, ext)
                }
            }
        }
    }

    abstract fun touchExtension(target: Project, ext: E)
}
