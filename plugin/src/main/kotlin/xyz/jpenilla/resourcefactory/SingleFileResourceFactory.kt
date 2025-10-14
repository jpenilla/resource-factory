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

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.nio.file.Path

abstract class SingleFileResourceFactory : ResourceFactory {
    /**
     * The path in the output directory to generate the file at.
     */
    @get:Input
    abstract val path: Property<String>

    /**
     * The behavior when a file already exists at the path.
     * Defaults to [DuplicatesMode.FAIL].
     */
    @get:Input
    abstract val duplicatesMode: Property<DuplicatesMode>

    /**
     * The behavior when a file already exists at the path.
     */
    enum class DuplicatesMode {
        /**
         * Ignore the existing file and invoke [generateSingleFile] regardless.
         */
        IGNORE,

        /**
         * Fail the build if a file already exists at the path.
         */
        FAIL
    }

    init {
        init()
    }

    private fun init() {
        duplicatesMode.convention(DuplicatesMode.FAIL)
    }

    /**
     * Generate the file at the path.
     *
     * @param outputFile the path to generate the file at
     */
    abstract fun generateSingleFile(outputFile: Path)

    override fun generate(outputDir: Path) {
        val outputFile = outputDir.resolve(path.get())
        if (duplicatesMode.get() == DuplicatesMode.FAIL) {
            if (outputFile.toFile().exists()) {
                throw GradleException("File '$outputFile' already exists, did another factory create it?")
            }
        }
        generateSingleFile(outputFile)
    }
}
