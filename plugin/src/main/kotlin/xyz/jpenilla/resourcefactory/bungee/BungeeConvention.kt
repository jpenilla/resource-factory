package xyz.jpenilla.resourcefactory.bungee

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BungeeConvention : ResourceFactoryConventionPlugin<BungeePluginYml>(
    "bungeePluginYml",
    { project -> project.bungeePluginYml() }
)
