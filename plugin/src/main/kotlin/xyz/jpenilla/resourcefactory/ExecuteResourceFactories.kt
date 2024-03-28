package xyz.jpenilla.resourcefactory

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.io.path.createDirectories

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
