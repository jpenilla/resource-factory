package xyz.jpenilla.resourcefactory.paper

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class PaperConvention : ResourceFactoryConventionPlugin.Provider<PaperPluginYml>(
    "paperPluginYml",
    { project -> project.paperPluginYml() }
)
