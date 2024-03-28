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
