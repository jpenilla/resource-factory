package xyz.jpenilla.resourcefactory.bungee

import org.gradle.api.Project
import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BungeeConvention : ResourceFactoryConventionPlugin<BungeePluginYml>(
    "bungeePluginYml",
    { project -> project.bungeePluginYml() },
    "main",
    { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
) {
    override fun touchExtension(target: Project, ext: BungeePluginYml) {
        target.afterEvaluate {
            // we need to set the conventions again, since the object was created eagerly.
            ext.copyProjectMeta(this)
        }
    }
}
