package xyz.jpenilla.resourcefactory.fabric

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

enum class Environment(private val serialized: String) {
    CLIENT("client"),
    SERVER("server"),
    ANY("*");

    object Serializer : TypeSerializer<Environment> {
        override fun deserialize(type: Type?, node: ConfigurationNode?): Environment {
            throw UnsupportedOperationException()
        }

        override fun serialize(type: Type, obj: Environment?, node: ConfigurationNode) {
            node.set(obj?.serialized)
        }
    }
}
