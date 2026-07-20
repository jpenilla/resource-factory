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
package xyz.jpenilla.resourcefactory

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject
import kotlin.io.path.createDirectories

/**
 * Executes configured [ResourceFactory]s.
 *
 * Caching is disabled by default because resource generation is generally inexpensive, making cache storage and
 * retrieval overhead unlikely to be worthwhile. Custom factories may also perform arbitrary work or rely on undeclared
 * inputs. This conservative default does not imply that the task cannot be cached: the built-in factories are
 * cache-compatible, and builds using only cache-compatible factories may opt in using
 * [org.gradle.api.tasks.TaskOutputs.cacheIf].
 */
@DisableCachingByDefault(because = "Resource generation is inexpensive and may perform arbitrary user-defined work")
abstract class ExecuteResourceFactories : DefaultTask() {
    /**
     * The [ResourceFactory]s to execute.
     */
    @get:Nested
    abstract val factories: ListProperty<ResourceFactory>

    /**
     * The directory to output generated resources to.
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val fsOps: FileSystemOperations

    @TaskAction
    fun run() {
        val dir = outputDir.get().asFile.toPath()
        fsOps.delete {
            delete(dir)
        }

        dir.createDirectories()

        val resourceFactories = factories.get()
        for ((index, factory) in resourceFactories.withIndex()) {
            try {
                factory.generate(dir)
            } catch (ex: Exception) {
                throw GradleException(
                    "Exception executing factory $index $factory: ${ex.message}\nFactories:\n${printFactories(resourceFactories)}",
                    ex
                )
            }
        }
    }

    private fun printFactories(resourceFactories: List<ResourceFactory>): String {
        val sb = StringBuilder()
        for ((index, resourceFactory) in resourceFactories.withIndex()) {
            sb.append(" $index. $resourceFactory\n")
        }
        return sb.toString()
    }
}
