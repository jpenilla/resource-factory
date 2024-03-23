package xyz.jpenilla.resourcefactory.velocity

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class VelocityConvention : ResourceFactoryConventionPlugin<VelocityPluginJson>(
    "velocityPluginJson",
    { project -> project.velocityPluginJson() },
    "main"
)
