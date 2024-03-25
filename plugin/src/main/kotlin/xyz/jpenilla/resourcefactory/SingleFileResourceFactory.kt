package xyz.jpenilla.resourcefactory

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.nio.file.Path

abstract class SingleFileResourceFactory : ResourceFactory {
    @get:Input
    abstract val path: Property<String>

    @get:Input
    abstract val duplicatesMode: Property<DuplicatesMode>

    enum class DuplicatesMode {
        IGNORE,
        FAIL
    }

    init {
        init()
    }

    private fun init() {
        duplicatesMode.convention(DuplicatesMode.FAIL)
    }

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
