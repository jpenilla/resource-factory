package xyz.jpenilla.resourcefactory

import java.nio.file.Path

interface ResourceFactory {
    fun generate(outputDir: Path)

    interface Provider {
        fun resourceFactory(): ResourceFactory
    }
}
