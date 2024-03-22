package xyz.jpenilla.resourcefactory.fabric

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.util.NamingSchemes
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.nullIfEmpty
import java.lang.reflect.Type
import java.nio.file.Path
import javax.inject.Inject

fun Project.fabricModJson(op: FabricModJson.() -> Unit = {}): FabricModJson {
    val yml = FabricModJson(objects)
    yml.copyProjectMeta(this)
    yml.op()
    return yml
}

open class FabricModJson constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.ObjectMapper.ValueProvider {

    @get:Input
    val id: Property<String> = objects.property()

    @get:Input
    val version: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val environment: Property<Environment> = objects.property()

    @get:Nested
    val entrypoints: ListProperty<Entrypoint> = objects.listProperty()

    fun mainEntrypoint(value: String, op: Entrypoint.() -> Unit = {}) =
        entrypoint("main", value, op)

    fun clientEntrypoint(value: String, op: Entrypoint.() -> Unit = {}) =
        entrypoint("client", value, op)

    fun serverEntrypoint(value: String, op: Entrypoint.() -> Unit = {}) =
        entrypoint("server", value, op)

    fun entrypoint(type: String, value: String, op: Entrypoint.() -> Unit = {}): Entrypoint {
        val ep = objects.newInstance(Entrypoint::class)
        ep.type.set(type)
        ep.value.set(value)
        ep.op()
        entrypoints.add(ep)
        return ep
    }

    @get:Input
    val languageAdapters: MapProperty<String, String> = objects.mapProperty()

    @get:Nested
    val mixins: ListProperty<MixinConfig> = objects.listProperty()

    fun mixin(name: String, op: MixinConfig.() -> Unit = {}): MixinConfig {
        val mixin = objects.newInstance(MixinConfig::class)
        mixin.config.set(name)
        mixin.op()
        mixins.add(mixin)
        return mixin
    }

    @get:Input
    @get:Optional
    val accessWidener: Property<String> = objects.property()

    @get:Input
    val depends: MapProperty<String, List<String>> = objects.mapProperty()

    fun depends(modId: String, vararg versionRanges: String) = depends.put(modId, versionRanges.toList())

    @get:Input
    val recommends: MapProperty<String, List<String>> = objects.mapProperty()

    fun recommends(modId: String, vararg versionRanges: String) = recommends.put(modId, versionRanges.toList())

    @get:Input
    val suggests: MapProperty<String, List<String>> = objects.mapProperty()

    fun suggests(modId: String, vararg versionRanges: String) = suggests.put(modId, versionRanges.toList())

    @get:Input
    val conflicts: MapProperty<String, List<String>> = objects.mapProperty()

    fun conflicts(modId: String, vararg versionRanges: String) = conflicts.put(modId, versionRanges.toList())

    @get:Input
    val breaks: MapProperty<String, List<String>> = objects.mapProperty()

    fun breaks(modId: String, vararg versionRanges: String) = breaks.put(modId, versionRanges.toList())

    @get:Input
    @get:Optional
    val name: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val description: Property<String> = objects.property()

    @get:Nested
    val authors: ListProperty<Person> = objects.listProperty()

    fun author(name: String, op: Person.() -> Unit = {}) = authors.add(person(name, op))

    @get:Nested
    val contributors: ListProperty<Person> = objects.listProperty()

    fun contributor(name: String, op: Person.() -> Unit = {}) = contributors.add(person(name, op))

    @get:Nested
    val contact: ContactInformation = objects.newInstance()

    fun contact(op: ContactInformation.() -> Unit) = contact.apply(op)

    @get:Input
    val license: ListProperty<String> = objects.listProperty()

    @get:Nested
    @get:Optional
    val icon: Property<Icon> = objects.property()

    /**
     * Copy the name, version, and description from the provided project.
     *
     * [project] project
     */
    fun copyProjectMeta(project: Project) {
        id.convention(project.name)
        name.convention(project.name)
        version.convention(project.version as String?)
        description.convention(project.description)
    }

    fun icon(vararg icons: Pair<String, String>) = icon.set(IconMap(mapOf(*icons)))

    fun icon(path: String) = icon.set(SingleIcon(path))

    interface Icon {
        object Serializer : TypeSerializer<Icon> {
            override fun deserialize(type: Type?, node: ConfigurationNode?): Icon {
                throw UnsupportedOperationException()
            }

            override fun serialize(type: Type, obj: Icon?, node: ConfigurationNode) {
                when (obj) {
                    is SingleIcon -> node.set(obj.path)
                    is IconMap -> node.set(obj.icons.toMap())
                    else -> throw UnsupportedOperationException("Cannot serialize $obj")
                }
            }
        }
    }

    class SingleIcon(
        @get:Input
        val path: String
    ) : Icon

    class IconMap(
        @get:Input
        val icons: Map<String, String>
    ) : Icon

    fun person(name: String, op: Person.() -> Unit = {}): Person = objects.newInstance<Person>()
        .apply { this.name.set(name) }
        .apply(op)

    abstract class Person @Inject constructor(objects: ObjectFactory) {
        @get:Input
        abstract val name: Property<String>

        @get:Nested
        val contact: ContactInformation = objects.newInstance()

        fun contact(op: ContactInformation.() -> Unit) = contact.apply(op)
    }

    abstract class ContactInformation {
        @get:Input
        @get:Optional
        abstract val email: Property<String>

        @get:Input
        @get:Optional
        abstract val irc: Property<String>

        @get:Input
        @get:Optional
        abstract val homepage: Property<String>

        @get:Input
        @get:Optional
        abstract val issues: Property<String>

        @get:Input
        @get:Optional
        abstract val sources: Property<String>

        @get:Input
        abstract val extra: MapProperty<String, String>

        fun asMap(): HashMap<String, String>? {
            val map = hashMapOf<String, String>()
            email.takeIf { it.isPresent }?.let { map["email"] = it.get() }
            irc.takeIf { it.isPresent }?.let { map["irc"] = it.get() }
            homepage.takeIf { it.isPresent }?.let { map["homepage"] = it.get() }
            issues.takeIf { it.isPresent }?.let { map["issues"] = it.get() }
            sources.takeIf { it.isPresent }?.let { map["sources"] = it.get() }
            map.putAll(extra.get())
            return map.takeIf { it.isNotEmpty() }
        }
    }

    abstract class Entrypoint {
        @get:Optional
        @get:Input
        abstract val adapter: Property<String>

        @get:Input
        abstract val value: Property<String>

        @get:Input
        abstract val type: Property<String>
    }

    abstract class MixinConfig {
        @get:Input
        abstract val config: Property<String>

        @get:Input
        @get:Optional
        abstract val environment: Property<Environment>
    }

    fun resourceFactory(): ResourceFactory {
        val gen = objects.newInstance(
            ConfigurateSingleFileResourceFactory.ObjectMapper::class,
            { path: Path ->
                GsonConfigurationLoader.builder()
                    .defaultOptions {
                        it.serializers { s ->
                            s.registerExact(Environment::class.java, Environment.Serializer)
                                .registerAnnotatedObjects(
                                    ObjectMapper.factoryBuilder()
                                        .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                                        .build()
                                )
                                .register(Icon::class.java, Icon.Serializer)
                        }
                    }
                    .path(path)
                    .build()
            }
        )
        gen.path.set("fabric.mod.json")
        gen.value.set(this)
        return gen
    }

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    @ConfigSerializable
    open class Serializable(fmj: FabricModJson) {
        val schemaVersion = 1
        val id = fmj.id.get()
        val version = fmj.version.get()
        val environment = fmj.environment.orNull
        val entrypoints = fmj.entrypoints.get().groupBy({ it.type.get() }) {
            SerializableEntrypoint(it.adapter.orNull, it.value.get())
        }
        val languageAdapters = fmj.languageAdapters.get().toMap()
        val mixins = fmj.mixins.nullIfEmpty()?.map { SerializableMixinConfig(it.config.get(), it.environment.orNull) }
        val accessWidener = fmj.accessWidener.orNull
        val depends = fmj.depends.nullIfEmpty()
        val recommends = fmj.recommends.nullIfEmpty()
        val suggests = fmj.suggests.nullIfEmpty()
        val conflicts = fmj.conflicts.nullIfEmpty()
        val breaks = fmj.breaks.nullIfEmpty()
        val name = fmj.name.orNull
        val description = fmj.description.orNull
        val authors = fmj.authors.nullIfEmpty()?.map { SerializablePerson(it.name.get(), it.contact.asMap()) }
        val contributors = fmj.contributors.nullIfEmpty()?.map { SerializablePerson(it.name.get(), it.contact.asMap()) }
        val contact = fmj.contact.asMap()
        val license = fmj.license.nullIfEmpty()
        val icon = fmj.icon.orNull
    }

    @ConfigSerializable
    class SerializableEntrypoint(
        val adapter: String?,
        val value: String
    )

    @ConfigSerializable
    class SerializableMixinConfig(
        val config: String,
        val environment: Environment?
    )

    @ConfigSerializable
    class SerializablePerson(
        val name: String,
        val contact: Map<String, String>?
    )
}
