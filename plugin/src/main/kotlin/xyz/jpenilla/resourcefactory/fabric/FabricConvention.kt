package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.Project
import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class FabricConvention : ResourceFactoryConventionPlugin<FabricModJson>(
    "fabricModJson",
    { project -> project.fabricModJson() },
    "main",
    { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
) {
    override fun touchExtension(target: Project, ext: FabricModJson) {
        target.afterEvaluate {
            // we need to set the conventions again, since the object was created eagerly.
            ext.copyProjectMeta(this)
        }
    }
}
