package xyz.jpenilla.resourcefactory.bukkit

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BukkitConvention : ResourceFactoryConventionPlugin.Provider<BukkitPluginYml>(
    "bukkitPluginYml",
    { project -> project.bukkitPluginYml() }
)
