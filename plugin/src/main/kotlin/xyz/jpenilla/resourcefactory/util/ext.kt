package xyz.jpenilla.resourcefactory.util

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty

fun <T> ListProperty<T>.nullIfEmpty(): List<T>? = if (get().isEmpty()) null else get().toList()

fun <T> SetProperty<T>.nullIfEmpty(): Set<T>? = if (get().isEmpty()) null else get().toSet()

fun <A, B> MapProperty<A, B>.nullIfEmpty(): Map<A, B>? = if (get().isEmpty()) null else get().toMap()

fun <A> NamedDomainObjectContainer<A>.nullIfEmpty(): Map<String, A>? = if (isEmpty()) null else asMap.toMap()
