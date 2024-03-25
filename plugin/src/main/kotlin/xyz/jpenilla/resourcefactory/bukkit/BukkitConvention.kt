package xyz.jpenilla.resourcefactory.bukkit

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BukkitConvention : ResourceFactoryConventionPlugin.Provider<BukkitPluginYaml>(
    "bukkitPluginYaml",
    { project -> project.bukkitPluginYaml() }
)
