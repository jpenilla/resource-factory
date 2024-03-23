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

abstract class ResourceFactoryConventionPlugin<E : ResourceFactory.Provider>(
    private val extensionName: String,
    private val extensionFactory: (Project) -> E,
    private val sourceSetName: String = "main"
) : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(ResourceFactoryPlugin::class)
        target.plugins.withType(JavaBasePlugin::class) {
            val ext = extensionFactory(target)
            touchExtension(target, ext)
            target.extensions.add(extensionName, ext)
            target.extensions.getByType(SourceSetContainer::class).named(sourceSetName) {
                extensions.configure(ResourceFactoryExtension::class) {
                    factory(ext.resourceFactory())
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
