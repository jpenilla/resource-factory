/*
 * Resource Factory Gradle Plugin
 * Copyright (c) 2024 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.tasks.Input

/**
 * A Mod Menu badge.
 *
 * Mod Menu currently only reads [LIBRARY] and [DEPRECATED] from mod metadata, and skips unknown keys
 * with a warning. Construct a badge from a key directly only for keys Mod Menu supports but that
 * aren't defined here.
 *
 * @property key the badge key written to the metadata
 */
data class ModMenuBadge(
    @get:Input
    val key: String,
) {
    companion object {
        @JvmField
        val LIBRARY: ModMenuBadge = ModMenuBadge("library")

        @JvmField
        val DEPRECATED: ModMenuBadge = ModMenuBadge("deprecated")
    }
}
