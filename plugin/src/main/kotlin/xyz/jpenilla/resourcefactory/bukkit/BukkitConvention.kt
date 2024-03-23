package xyz.jpenilla.resourcefactory.bukkit

import org.gradle.api.Project
import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BukkitConvention : ResourceFactoryConventionPlugin<BukkitPluginYml>(
    "bukkitPluginYml",
    { project -> project.bukkitPluginYml() },
    "main",
    { factoryExt, ext -> factoryExt.factory(ext.resourceFactory()) }
) {
    override fun touchExtension(target: Project, ext: BukkitPluginYml) {
        target.afterEvaluate {
            // we need to set the conventions again, since the object was created eagerly.
            ext.copyProjectMeta(this)
        }
    }
}
