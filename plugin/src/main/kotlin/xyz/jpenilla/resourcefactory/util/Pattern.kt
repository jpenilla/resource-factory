package xyz.jpenilla.resourcefactory.util

import org.intellij.lang.annotations.Language

@Target(AnnotationTarget.PROPERTY)
annotation class Pattern(
    @Language("RegExp")
    val pattern: String,
    val description: String = ""
)
