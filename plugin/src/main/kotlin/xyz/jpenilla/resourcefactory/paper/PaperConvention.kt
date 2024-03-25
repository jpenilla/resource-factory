package xyz.jpenilla.resourcefactory.paper

import xyz.jpenilla.resourcefactory.ResourceFactoryConventionPlugin

abstract class PaperConvention : ResourceFactoryConventionPlugin.Provider<PaperPluginYaml>(
    "paperPluginYaml",
    { project -> project.paperPluginYaml() }
)
