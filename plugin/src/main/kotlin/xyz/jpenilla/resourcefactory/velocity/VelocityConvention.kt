package xyz.jpenilla.resourcefactory.velocity

import org.gradle.api.Project
import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class VelocityConvention : ResourceFactoryConventionPlugin<VelocityPluginJson>(
    "velocityPluginJson",
    { project -> project.velocityPluginJson() },
    "main",
    { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
) {
    override fun touchExtension(target: Project, ext: VelocityPluginJson) {
        target.afterEvaluate {
            // we need to set the conventions again, since the object was created eagerly.
            ext.copyProjectMeta(this)
        }
    }
}
