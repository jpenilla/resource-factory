package xyz.jpenilla.resourcefactory.bungee

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BungeeConvention : ResourceFactoryConventionPlugin.Provider<BungeePluginYml>(
    "bungeePluginYml",
    { project -> project.bungeePluginYml() }
)
