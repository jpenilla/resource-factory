package xyz.jpenilla.resourcefactory.util

import org.gradle.api.Project

/**
 * Implemented by types that can set conventions from a [Project]'s meta.
 */
interface ProjectMetaConventions {
    /**
     * Set conventions from the given [Project]'s meta.
     *
     * This typically includes name, version, and description.
     *
     * @param project the project to set conventions from
     */
    fun setConventionsFromProjectMeta(project: Project)
}
