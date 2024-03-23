package xyz.jpenilla.resourcefactory.bukkit

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializer
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import java.lang.reflect.Type

class Permission(objects: ObjectFactory, @Input val name: String) {
    @get:Input
    @get:Optional
    val description: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val default: Property<Default> = objects.property()

    @get:Input
    val children: MapProperty<String, Boolean> = objects.mapProperty()

    fun children(vararg nodes: String) {
        for (node in nodes) {
            children.put(node, true)
        }
    }

    enum class Default(val serialized: String) {
        TRUE("true"),
        FALSE("false"),
        OP("op"),
        NOT_OP("not op");

        object Serializer : TypeSerializer<Default> {
            override fun deserialize(type: Type?, node: ConfigurationNode?): Default {
                throw UnsupportedOperationException()
            }

            override fun serialize(type: Type, obj: Default?, node: ConfigurationNode) {
                node.set(obj?.serialized)
            }
        }
    }

    @ConfigSerializable
    class Serializable(permission: Permission) {
        val description = permission.description.orNull
        val default = permission.default.orNull
        val children = permission.children.nullIfEmpty()
    }
}
