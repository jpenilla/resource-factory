package xyz.jpenilla.resourcefactory.util

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

fun <T> ListProperty<T>.nullIfEmpty(): List<T>? = if (get().isEmpty()) null else get().toList()

fun <T> SetProperty<T>.nullIfEmpty(): Set<T>? = if (get().isEmpty()) null else get().toSet()

fun <A, B> MapProperty<A, B>.nullIfEmpty(): Map<A, B>? = if (get().isEmpty()) null else get().toMap()

fun <A> NamedDomainObjectContainer<A>.nullIfEmpty(): Map<String, A>? = if (isEmpty()) null else asMap.toMap()

fun KProperty<String>.validate(): String =
    orNullValidating { it } ?: throw NullPointerException()

fun KProperty<Property<String>>.getValidating(): String =
    orNullValidating { it.get() } ?: throw NullPointerException()

fun KProperty<Property<String>>.orNullValidating(): String? = orNullValidating { it.orNull }

fun <T : Any?> KProperty<T>.orNullValidating(
    stringGetter: (T) -> String?,
): String? {
    val value = stringGetter(getter.call())
    val declrCls = javaField?.declaringClass?.simpleName
        ?: javaGetter?.declaringClass?.simpleName
        ?: throw IllegalArgumentException("Cannot find owning class for property $this")
    val fallbackDesc = "$declrCls.$name"
    val annotation = findAnnotation<xyz.jpenilla.resourcefactory.util.Pattern>()
        ?: throw GradleException("Property $fallbackDesc is not annotated with @Pattern.")
    return value?.validate(
        annotation.pattern,
        annotation.description.takeIf { it.isNotBlank() } ?: fallbackDesc
    )
}

fun Collection<String>.validateAll(@Language("RegExp") pattern: String, description: String): Collection<String> {
    for (string in this) {
        string.validate(pattern, description)
    }
    return this
}

fun String.validate(@Language("RegExp") pattern: String, description: String): String {
    val regex = Pattern.compile(pattern)
    if (!regex.matcher(this).matches()) {
        throw GradleException("Invalid $description '$this', must match pattern '$pattern'.")
    }
    return this
}
