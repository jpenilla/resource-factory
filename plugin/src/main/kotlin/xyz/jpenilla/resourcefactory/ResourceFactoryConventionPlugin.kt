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
