package xyz.jpenilla.resourcefactory

import java.nio.file.Path

/**
 * A factory that generates resources.
 */
interface ResourceFactory {
    /**
     * Generate the resources.
     *
     * @param outputDir the directory to generate the resources in
     */
    fun generate(outputDir: Path)

    /**
     * A provider for [ResourceFactory]s.
     */
    interface Provider {
        /**
         * Get the provided [ResourceFactory].
         *
         * @return the [ResourceFactory]
         */
        fun resourceFactory(): ResourceFactory
    }
}
