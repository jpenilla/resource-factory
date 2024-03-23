package xyz.jpenilla.resourcefactory.util

import org.gradle.api.Action

object NullAction : Action<Any> {
    override fun execute(t: Any) {
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> nullAction(): Action<T> = NullAction as Action<T>
