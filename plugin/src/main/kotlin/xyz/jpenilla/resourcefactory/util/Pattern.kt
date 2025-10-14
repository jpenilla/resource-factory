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
