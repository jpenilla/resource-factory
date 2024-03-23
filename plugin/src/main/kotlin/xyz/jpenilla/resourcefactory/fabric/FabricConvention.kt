package xyz.jpenilla.resourcefactory.fabric

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class FabricConvention : ResourceFactoryConventionPlugin.Provider<FabricModJson>(
    "fabricModJson",
    { project -> project.fabricModJson() }
)
