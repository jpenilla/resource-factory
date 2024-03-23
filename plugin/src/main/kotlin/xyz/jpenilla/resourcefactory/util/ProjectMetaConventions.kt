package xyz.jpenilla.resourcefactory.util

import org.gradle.api.Project

interface ProjectMetaConventions {
    fun setConventionsFromProjectMeta(project: Project)
}