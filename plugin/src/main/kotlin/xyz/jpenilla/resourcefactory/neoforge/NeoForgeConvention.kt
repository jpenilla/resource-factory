package xyz.jpenilla.resourcefactory.neoforge

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class NeoForgeConvention : ResourceFactoryConventionPlugin.Provider<NeoForgeModsToml>(
    "neoForgeModsToml",
    { project -> project.neoForgeModsToml() }
)
