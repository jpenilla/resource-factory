package xyz.jpenilla.resourcefactory.util

import org.intellij.lang.annotations.Language

/**
 * Annotates properties to indicate that their value(s), if present, must match a regular expression pattern.
 *
 * @param pattern the regular expression pattern to match
 * @param description a description of the pattern, used in validation error messages
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Pattern(
    @Language("RegExp")
    val pattern: String,
    val description: String = ""
)
