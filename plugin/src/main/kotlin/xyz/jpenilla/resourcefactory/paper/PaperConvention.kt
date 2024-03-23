package xyz.jpenilla.resourcefactory.paper

import org.gradle.api.Project
import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class PaperConvention : ResourceFactoryConventionPlugin<PaperPluginYml>(
    "paperPluginYml",
    { project -> project.paperPluginYml() },
    "main",
    { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
) {
    override fun touchExtension(target: Project, ext: PaperPluginYml) {
        target.afterEvaluate {
            // we need to set the conventions again, since the object was created eagerly.
            ext.copyProjectMeta(this)
        }
    }
}
