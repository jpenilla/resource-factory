package xyz.jpenilla.resourcefactory.util

import io.leangen.geantyref.TypeFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.function.Function

abstract class CustomValueProvider<T : Any>(
    @get:Internal
    val type: Type?
) {
    abstract fun value(): T
}

object CustomValues : CustomValueFactory()

abstract class CustomValueFactory {
    protected constructor()

    /**
     * Creates a value provider for a complex value. The value is exposed to Gradle as a nested property,
     * and the mapper function is used to extract the serializable value.
     *
     * @param V the type of the complex value
     * @param S the type of the serializable value
     * @param value complex value
     * @param mapper mapper from complex value to serializable value
     * @return the created value provider
     */
    fun <V : Any, S : Any> complexCustomValue(value: V, mapper: Function<V, S>): CustomValueProvider<S> =
        typedComplexCustomValue(null, value, mapper)

    /**
     * Creates a value provider for a complex value that satisfies Configurate and Gradle's task input model.
     * The value is exposed to Gradle as a nested property.
     *
     * @param V the type of the complex value
     * @param value complex value
     * @return the created value provider
     */
    fun <V : Any> complexCustomValue(value: V): CustomValueProvider<V> =
        typedComplexCustomValue(null, value) { it }

    /**
     * Creates a value provider for a complex value. The value is exposed to Gradle as a nested property,
     * and the mapper function is used to extract the serializable value.
     *
     * This 'typed' variant is useful when the serialized type has a generic parameter.
     *
     * @param V the type of the complex value
     * @param S the type of the serializable value
     * @param value complex value
     * @param mapper mapper from complex value to serializable value
     * @return the created value provider
     */
    fun <V : Any, S : Any> typedComplexCustomValue(type: Type?, value: V, mapper: Function<V, S>): CustomValueProvider<S> =
        object : CustomValueProvider<S>(type) {
            @get:Nested
            val value = value

            @get:Nested
            val mapper = mapper

            override fun value(): S = this.mapper.apply(this.value)
        }

    /**
     * Creates a value provider for a complex value that satisfies Configurate and Gradle's task input model.
     * The value is exposed to Gradle as a nested property.
     *
     * This 'typed' variant is useful when the serialized type has a generic parameter.
     *
     * @param V the type of the complex value
     * @param value complex value
     * @return the created value provider
     */
    fun <V : Any> typedComplexCustomValue(type: Type?, value: V): CustomValueProvider<V> =
        typedComplexCustomValue(type, value) { it }

    /**
     * Creates a value provider for a simple value. The value is exposed to Gradle as an input property.
     *
     * @param T the type of the value
     * @param value the simple value
     * @return the created value provider
     */
    fun <T : Any> simpleCustomValue(value: T): CustomValueProvider<T> =
        typedSimpleCustomValue(null, value)

    /**
     * Creates a value provider for a simple value. The value is exposed to Gradle as an input property.
     *
     * This 'typed' variant is useful when the serialized type has a generic parameter.
     *
     * @param T the type of the value
     * @param value the simple value
     * @return the created value provider
     */
    fun <T : Any> typedSimpleCustomValue(type: Type?, value: T): CustomValueProvider<T> =
        object : CustomValueProvider<T>(type) {
            @get:Input
            val value = value

            override fun value(): T {
                return this.value
            }
        }

    /**
     * Creates a value provider for a simple map value.
     *
     * @param K the type of the map key
     * @param V the type of the map value
     * @param value the simple map
     * @return the created value provider
     * @see [typedSimpleCustomValue]
     */
    fun <K : Any, V : Any> simpleCustomValueMap(keyType: Type, valueType: Type, value: Map<K, V>): CustomValueProvider<Map<K, V>> =
        typedSimpleCustomValue(TypeFactory.parameterizedClass(Map::class.java, keyType, valueType), value)

    /**
     * Creates a value provider for a simple map value.
     *
     * @param K the type of the map key
     * @param V the type of the map value
     * @param value the simple map
     * @return the created value provider
     * @see [typedSimpleCustomValue]
     */
    inline fun <reified K : Any, reified V : Any> simpleCustomValueMap(value: Map<K, V>): CustomValueProvider<Map<K, V>> =
        simpleCustomValueMap(K::class.java, V::class.java, value)

    /**
     * Creates a value provider for a simple list value.
     *
     * @param V the type of the list value
     * @param value the simple list
     * @return the created value provider
     * @see [typedSimpleCustomValue]
     */
    fun <V : Any> simpleCustomValueList(valueType: Type, value: List<V>): CustomValueProvider<List<V>> =
        typedSimpleCustomValue(TypeFactory.parameterizedClass(List::class.java, valueType), value)

    /**
     * Creates a value provider for a simple list value.
     *
     * @param V the type of the list value
     * @param value the simple list
     * @return the created value provider
     * @see [typedSimpleCustomValue]
     */
    inline fun <reified V : Any> simpleCustomValueList(value: List<V>): CustomValueProvider<List<V>> =
        simpleCustomValueList(V::class.java, value)
}

object CustomValueProviderSerializer : TypeSerializer<CustomValueProvider<*>> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): CustomValueProvider<*> {
        throw UnsupportedOperationException()
    }

    override fun serialize(type: Type, obj: CustomValueProvider<*>?, node: ConfigurationNode) {
        if (obj != null) {
            if (obj.type == null) {
                val value = obj.value()
                node.set(value::class.java, value)
            } else {
                node.set(obj.type, obj.value())
            }
        } else {
            node.set(null)
        }
    }
}
