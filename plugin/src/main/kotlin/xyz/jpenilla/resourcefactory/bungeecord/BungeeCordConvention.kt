package xyz.jpenilla.resourcefactory.bungeecord

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class BungeeCordConvention : ResourceFactoryConventionPlugin.Provider<BungeeCordPluginYaml>(
    "bungeePluginYaml",
    { project -> project.bungeePluginYaml() }
)
