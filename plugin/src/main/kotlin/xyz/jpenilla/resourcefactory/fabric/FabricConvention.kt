package xyz.jpenilla.resourcefactory.fabric

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class FabricConvention : ResourceFactoryConventionPlugin<FabricModJson>(
    "fabricModJson",
    { project -> project.fabricModJson() },
    "main"
)
