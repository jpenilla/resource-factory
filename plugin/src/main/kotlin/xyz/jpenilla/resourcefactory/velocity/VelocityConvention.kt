package xyz.jpenilla.resourcefactory.velocity

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class VelocityConvention : ResourceFactoryConventionPlugin.Provider<VelocityPluginJson>(
    "velocityPluginJson",
    { project -> project.velocityPluginJson() }
)
