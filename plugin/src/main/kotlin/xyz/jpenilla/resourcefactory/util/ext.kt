package xyz.jpenilla.resourcefactory.util

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
fun <T, C : Collection<T>> Provider<C>.nullIfEmpty(): C? = get().let {
    if (it.isEmpty()) return@let null

    return@let when (it) {
        is List<*> -> it.toList()
        is Set<*> -> it.toSet()
        else -> return@let it
    }
} as C?

fun <A, B> MapProperty<A, B>.nullIfEmpty(): Map<A, B>? = if (get().isEmpty()) null else get().toMap()

fun <A> NamedDomainObjectContainer<A>.nullIfEmpty(): Map<String, A>? = if (isEmpty()) null else asMap.toMap()

fun KProperty<String>.validate(): String =
    orNullValidating { it } ?: throw NullPointerException()

fun KProperty<Property<String>>.getValidating(): String =
    orNullValidating { it.get() } ?: throw NullPointerException()

fun KProperty<Property<String>>.orNullValidating(): String? = orNullValidating { it.orNull }

private fun <T : Any?> KProperty<T>.orNullValidating(
    stringGetter: (T) -> String?,
): String? {
    val value = stringGetter(getter.call())
    val annotation = patternAnnotation()
    return value?.validate(annotation.pattern, annotation.description.takeIf { it.isNotBlank() } ?: fallbackDescription())
}

fun <T : Collection<String>> KProperty<Provider<T>>.nullIfEmptyValidating(): T? {
    val value = getter.call().nullIfEmpty()
    val annotation = patternAnnotation()
    return value?.validateAll(annotation.pattern, annotation.description.takeIf { it.isNotBlank() } ?: fallbackDescription())
}

fun <C : Collection<String>> C.validateAll(@Language("RegExp") pattern: String, description: String): C = apply {
    for (string in this) {
        string.validate(pattern, description)
    }
}

fun String.validate(@Language("RegExp") pattern: String, description: String): String {
    val regex = Pattern.compile(pattern)
    if (!regex.matcher(this).matches()) {
        throw GradleException("Invalid $description '$this', must match pattern '$pattern'.")
    }
    return this
}

private fun KProperty<*>.patternAnnotation(): xyz.jpenilla.resourcefactory.util.Pattern =
    findAnnotation<xyz.jpenilla.resourcefactory.util.Pattern>()
        ?: throw GradleException("Property ${fallbackDescription()} is not annotated with @Pattern.")

private fun KProperty<*>.fallbackDescription(): String {
    val declrCls = javaField?.declaringClass?.simpleName
        ?: javaGetter?.declaringClass?.simpleName
        ?: throw IllegalArgumentException("Cannot find owning class for property $this")
    return "$declrCls.$name"
}
