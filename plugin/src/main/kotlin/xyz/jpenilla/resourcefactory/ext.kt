package xyz.jpenilla.resourcefactory

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty

fun <T> ListProperty<T>.nullIfEmpty(): List<T>? = if (get().isEmpty()) null else get().toList()

fun <A, B> MapProperty<A, B>.nullIfEmpty(): Map<A, B>? = if (get().isEmpty()) null else get().toMap()

fun <A> NamedDomainObjectContainer<A>.nullIfEmpty(): Map<String, A>? = if (isEmpty()) null else asMap.toMap()
