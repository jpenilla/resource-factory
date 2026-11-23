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
package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializer
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.validate
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * Mod Menu metadata.
 *
 * See [Mod Menu's documentation](https://github.com/TerraformersMC/ModMenu#fabric-metadata-api) for more information.
 */
abstract class ModMenu @Inject constructor(
    @Transient
    private val objects: ObjectFactory,
) {
    /**
     * The mod's badges.
     */
    @get:Nested
    abstract val badges: ListProperty<ModMenuBadge>

    fun badge(vararg badges: ModMenuBadge) = this.badges.addAll(*badges)

    /**
     * Adds the [ModMenuBadge.LIBRARY] badge.
     */
    fun library() = badge(ModMenuBadge.LIBRARY)

    /**
     * Adds the [ModMenuBadge.DEPRECATED] badge.
     */
    fun deprecated() = badge(ModMenuBadge.DEPRECATED)

    /**
     * Links shown by Mod Menu, from translation key to URL. The key is the translation key for the link's
     * label; see [ModMenuBuiltInLinks] for the keys Mod Menu provides translations for.
     */
    @get:Input
    abstract val links: MapProperty<String, String>

    /**
     * Helpers for adding [links] with the keys Mod Menu provides translations for.
     */
    @get:Internal
    val builtInLinks: ModMenuBuiltInLinks
        get() = ModMenuBuiltInLinks(links)

    fun builtInLinks(configure: Action<ModMenuBuiltInLinks>) = configure.execute(builtInLinks)

    /**
     * Whether Mod Menu's update checker is enabled for this mod (`update_checker`).
     */
    @get:Input
    @get:Optional
    abstract val updateChecker: Property<Boolean>

    /**
     * The parent mod, see the `parent` and `dummyParent` functions.
     */
    @get:Nested
    @get:Optional
    abstract val parent: Property<Parent>

    /**
     * Sets the parent to the mod with id [id].
     *
     * @param id parent mod id
     */
    fun parent(id: String) = parent.set(ParentId(objects.property<String>().value(id)))

    /**
     * Sets the parent to the mod described by [other], using its [FabricModJson.id] (read lazily).
     *
     * @param other parent mod metadata
     */
    fun parent(other: FabricModJson) = parent.set(ParentId(other.id))

    /**
     * Sets the parent to a dummy parent configured by [configure]. Mod Menu uses the dummy parent
     * when no mod with its id is installed.
     *
     * @param configure the block to configure the dummy parent with
     * @return the dummy parent
     */
    fun dummyParent(configure: Action<DummyParent>): DummyParent {
        val dummy = objects.newInstance<DummyParent>()
        configure.execute(dummy)
        parent.set(dummy)
        return dummy
    }

    /**
     * Sets the parent to a dummy parent filled lazily from [other]'s id, name, description, and
     * Mod Menu badges, then configured by [configure], which can override any copied field.
     *
     * The icon is not copied, since Mod Menu loads a dummy parent's icon from this (the child) mod's jar,
     * not the parent's. Set [DummyParent.icon] in [configure] if needed.
     *
     * @param other the metadata to fill the dummy parent from
     * @param configure the block to configure the dummy parent with
     * @return the dummy parent
     */
    @JvmOverloads
    fun dummyParent(other: FabricModJson, configure: Action<DummyParent> = nullAction()): DummyParent = dummyParent {
        id.set(other.id)
        name.set(other.name)
        description.set(other.description)
        badges.set(other.modMenu.badges)
        configure.execute(this)
    }

    interface Parent

    class ParentId(
        @get:Input
        val id: Provider<String>,
    ) : Parent

    /**
     * A Mod Menu dummy parent.
     */
    abstract class DummyParent : Parent {
        @get:Input
        abstract val id: Property<String>

        @get:Input
        @get:Optional
        abstract val name: Property<String>

        @get:Input
        @get:Optional
        abstract val description: Property<String>

        /**
         * The icon path, loaded from the child mod's jar. The file must exist there, or Mod Menu is likely
         * to crash when showing the mod list. `inherit` uses the child mod's icon. When unset, Mod Menu uses
         * its default parent icon.
         */
        @get:Input
        @get:Optional
        abstract val icon: Property<String>

        @get:Nested
        abstract val badges: ListProperty<ModMenuBadge>

        fun badge(vararg badges: ModMenuBadge) = this.badges.addAll(*badges)

        /**
         * Adds the [ModMenuBadge.LIBRARY] badge.
         */
        fun library() = badge(ModMenuBadge.LIBRARY)

        /**
         * Adds the [ModMenuBadge.DEPRECATED] badge.
         */
        fun deprecated() = badge(ModMenuBadge.DEPRECATED)
    }

    fun asSerializable(): SerializableModMenu? {
        val serializable = SerializableModMenu(
            badges.nullIfEmpty()?.map { it.key },
            links.nullIfEmpty(),
            updateChecker.orNull,
            when (val p = parent.orNull) {
                null -> null
                is ParentId -> SerializableParentId(p.id.get().validate(FabricModJson.MOD_ID_PATTERN, "Fabric mod id (of Mod Menu parent)"))
                is DummyParent -> SerializableDummyParent(
                    p.id.get().validate(FabricModJson.MOD_ID_PATTERN, "Fabric mod id (of Mod Menu dummy parent)"),
                    p.name.orNull,
                    p.description.orNull,
                    p.icon.orNull,
                    p.badges.nullIfEmpty()?.map { it.key },
                )
                else -> throw UnsupportedOperationException("Cannot serialize $p")
            },
        )
        return serializable.takeIf {
            it.badges != null || it.links != null || it.updateChecker != null || it.parent != null
        }
    }

    @ConfigSerializable
    class SerializableModMenu(
        val badges: List<String>?,
        val links: Map<String, String>?,
        @field:Setting("update_checker")
        val updateChecker: Boolean?,
        val parent: SerializableParent?,
    )

    interface SerializableParent {
        object Serializer : TypeSerializer<SerializableParent> {
            override fun deserialize(type: Type?, node: ConfigurationNode?): SerializableParent = throw UnsupportedOperationException()

            override fun serialize(type: Type, obj: SerializableParent?, node: ConfigurationNode) {
                when (obj) {
                    null -> node.set(null)
                    is SerializableParentId -> node.set(obj.id)
                    is SerializableDummyParent -> node.set(SerializableDummyParent::class.java, obj)
                    else -> throw UnsupportedOperationException("Cannot serialize $obj")
                }
            }
        }
    }

    class SerializableParentId(val id: String) : SerializableParent

    @ConfigSerializable
    class SerializableDummyParent(
        val id: String,
        val name: String?,
        val description: String?,
        val icon: String?,
        val badges: List<String>?,
    ) : SerializableParent
}
