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

import org.gradle.api.Action

/**
 * An action that does nothing.
 */
object NullAction : Action<Any> {
    override fun execute(t: Any) {
    }
}

/**
 * Returns an action that does nothing.
 *
 * @return an action that does nothing
 */
@Suppress("UNCHECKED_CAST")
fun <T> nullAction(): Action<T> = NullAction as Action<T>
