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
