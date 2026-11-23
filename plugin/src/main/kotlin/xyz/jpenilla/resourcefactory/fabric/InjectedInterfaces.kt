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
package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

/**
 * Loom interface injection metadata.
 *
 * @see FabricModJson.injectedInterfaces
 */
abstract class InjectedInterfaces {
    @get:Nested
    abstract val injections: ListProperty<Injection>

    /**
     * Injects [interfaces] into [target]. Repeated calls for the same target add to its list.
     *
     * Names are passed through unchanged, so they must be in Loom's form
     * (`/` separators, `$` for nested classes, e.g. `com/example/Outer$Inner`).
     *
     * @param target the class to inject into
     * @param interfaces the interfaces to inject
     */
    fun inject(target: String, vararg interfaces: String) = inject(target, interfaces.asList())

    /**
     * Injects [interfaces] into [target]. Repeated calls for the same target add to its list.
     *
     * Names are passed through unchanged, so they must be in Loom's form
     * (`/` separators, `$` for nested classes, e.g. `com/example/Outer$Inner`).
     *
     * @param target the class to inject into
     * @param interfaces the interfaces to inject
     */
    fun inject(target: String, interfaces: Iterable<String>) {
        val list = interfaces.toList()
        require(list.isNotEmpty()) { "No interfaces given to inject into '$target'" }
        injections.add(Injection(target, list))
    }

    class Injection(
        @get:Input
        val target: String,
        @get:Input
        val interfaces: List<String>,
    )

    fun asMap(): Map<String, List<String>>? = injections.get()
        .groupBy({ it.target }) { it.interfaces }
        .mapValues { (_, lists) -> lists.flatten().distinct() }
        .takeIf { it.isNotEmpty() }
}
