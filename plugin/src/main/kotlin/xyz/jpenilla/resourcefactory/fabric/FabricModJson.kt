package xyz.jpenilla.resourcefactory.fabric

import io.leangen.geantyref.TypeToken
import org.gradle.api.Action
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
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.util.NamingSchemes
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactoryExtension
import xyz.jpenilla.resourcefactory.util.ConfigurateCustomValueProviderSerializer
import xyz.jpenilla.resourcefactory.util.CustomValueFactory
import xyz.jpenilla.resourcefactory.util.CustomValueProvider
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.getValidating
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.validateAll
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * Create a [FabricModJson] and configure it with the given [configure] block.
 *
 * The created [FabricModJson] will inherit the project's name (as [FabricModJson.id] and [FabricModJson.name]), version, and description.
 *
 * @param configure the block to configure the [FabricModJson] with
 * @return the created and configured [FabricModJson]
 */
fun Project.fabricModJson(configure: Action<FabricModJson> = nullAction()): FabricModJson {
    val json = objects.newInstance<FabricModJson>()
    json.setConventionsFromProjectMeta(this)
    configure.execute(json)
    return json
}

/**
 * A `fabric.mod.json` configuration.
 *
 * See [the official spec](https://fabricmc.net/wiki/documentation:fabric_mod_json_spec) for more information.
 *
 * @see [fabricModJson]
 * @see [ResourceFactoryExtension.fabricModJson]
 */
abstract class FabricModJson @Inject constructor(
    @Transient
    private val objects: ObjectFactory
) : ConfigurateSingleFileResourceFactory.Simple.ValueProvider, ProjectMetaConventions, ResourceFactory.Provider, CustomValueFactory() {

    companion object {
        private const val MOD_ID_PATTERN: String = "^[a-z][a-z0-9-_]{1,63}$"
        private const val FILE_NAME: String = "fabric.mod.json"
    }

    @Pattern(MOD_ID_PATTERN, "Fabric mod id")
    @get:Input
    val id: Property<String> = objects.property()

    @get:Input
    val version: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val environment: Property<Environment> = objects.property()

    @get:Nested
    val entrypoints: ListProperty<Entrypoint> = objects.listProperty()

    @JvmOverloads
    fun mainEntrypoint(value: String, configure: Action<Entrypoint> = nullAction()) =
        entrypoint("main", value, configure)

    @JvmOverloads
    fun clientEntrypoint(value: String, configure: Action<Entrypoint> = nullAction()) =
        entrypoint("client", value, configure)

    @JvmOverloads
    fun serverEntrypoint(value: String, configure: Action<Entrypoint> = nullAction()) =
        entrypoint("server", value, configure)

    @JvmOverloads
    fun entrypoint(type: String, value: String, configure: Action<Entrypoint> = nullAction()): Entrypoint {
        val ep = objects.newInstance(Entrypoint::class)
        ep.type.set(type)
        ep.value.set(value)
        configure.execute(ep)
        entrypoints.add(ep)
        return ep
    }

    @get:Input
    val languageAdapters: MapProperty<String, String> = objects.mapProperty()

    @get:Nested
    val mixins: ListProperty<MixinConfig> = objects.listProperty()

    @JvmOverloads
    fun mixin(name: String, configure: Action<MixinConfig> = nullAction()): MixinConfig {
        val mixin = objects.newInstance(MixinConfig::class)
        mixin.config.set(name)
        configure.execute(mixin)
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

    @JvmOverloads
    fun author(name: String, configure: Action<Person> = nullAction()) = authors.add(person(name, configure))

    @get:Nested
    val contributors: ListProperty<Person> = objects.listProperty()

    @JvmOverloads
    fun contributor(name: String, configure: Action<Person> = nullAction()) = contributors.add(person(name, configure))

    @get:Nested
    val contact: ContactInformation = objects.newInstance()

    fun contact(configure: Action<ContactInformation>) = contact.apply { configure.execute(this) }

    @get:Input
    val license: ListProperty<String> = objects.listProperty()

    fun license(vararg licenses: String) = license.addAll(*licenses)

    fun apache2License() = license("Apache-2.0")

    fun mitLicense() = license("MIT")

    @get:Nested
    @get:Optional
    val icon: Property<Icon> = objects.property()

    /**
     * Configures the `custom` section of the [FabricModJson].
     *
     * @see [simpleCustomValue]
     * @see [typedSimpleCustomValue]
     * @see [complexCustomValue]
     * @see [typedComplexCustomValue]
     * @see [simpleCustomValueMap]
     * @see [simpleCustomValueList]
     */
    @get:Nested
    @get:Optional
    val custom: MapProperty<String, CustomValueProvider<*>> = objects.mapProperty()

    fun <T : Any> custom(name: String, value: CustomValueProvider<T>) = custom.put(name, value)

    override fun setConventionsFromProjectMeta(project: Project) {
        id.convention(project.name)
        name.convention(project.name)
        version.convention(project.version.toString())
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

    @JvmOverloads
    fun person(name: String, configure: Action<Person> = nullAction()): Person = objects.newInstance<Person>().apply {
        this.name.set(name)
        configure.execute(this)
    }

    abstract class Person @Inject constructor(objects: ObjectFactory) {
        @get:Input
        abstract val name: Property<String>

        @get:Nested
        val contact: ContactInformation = objects.newInstance()

        fun contact(configure: Action<ContactInformation>) = contact.apply { configure.execute(this) }
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

        fun asMap(): Map<String, String>? {
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

    override fun resourceFactory(): ResourceFactory {
        val gen = objects.newInstance(ConfigurateSingleFileResourceFactory.Simple::class)
        gen.json {
            defaultOptions {
                it.serializers { s ->
                    s.registerExact(Environment::class.java, Environment.Serializer)
                        .registerAnnotatedObjects(
                            ObjectMapper.factoryBuilder()
                                .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                                .build()
                        )
                        .register(object : TypeToken<CustomValueProvider<*>>() {}, ConfigurateCustomValueProviderSerializer)
                        .register(Icon::class.java, Icon.Serializer)
                }
            }
        }
        gen.path.set(FILE_NAME)
        gen.value.set(this)
        return gen
    }

    override fun asConfigSerializable(): Any {
        return Serializable(this)
    }

    @ConfigSerializable
    open class Serializable(fmj: FabricModJson) {
        val schemaVersion = 1
        val id = fmj::id.getValidating()
        val version = fmj.version.get()
        val environment = fmj.environment.orNull
        val entrypoints = fmj.entrypoints.get().groupBy({ it.type.get() }) {
            SerializableEntrypoint(it.adapter.orNull, it.value.get())
        }
        val languageAdapters = fmj.languageAdapters.nullIfEmpty()
        val mixins = fmj.mixins.nullIfEmpty()?.map { SerializableMixinConfig(it.config.get(), it.environment.orNull) }
        val accessWidener = fmj.accessWidener.orNull
        val depends = fmj.depends.nullIfEmpty()?.also { it.keys.validateAll(MOD_ID_PATTERN, "Fabric mod id (of depends)") }
        val recommends = fmj.recommends.nullIfEmpty()?.also { it.keys.validateAll(MOD_ID_PATTERN, "Fabric mod id (of recommends)") }
        val suggests = fmj.suggests.nullIfEmpty()?.also { it.keys.validateAll(MOD_ID_PATTERN, "Fabric mod id (of suggests)") }
        val conflicts = fmj.conflicts.nullIfEmpty()?.also { it.keys.validateAll(MOD_ID_PATTERN, "Fabric mod id (of conflicts)") }
        val breaks = fmj.breaks.nullIfEmpty()?.also { it.keys.validateAll(MOD_ID_PATTERN, "Fabric mod id (of breaks)") }
        val name = fmj.name.orNull
        val description = fmj.description.orNull
        val authors = fmj.authors.nullIfEmpty()?.map { SerializablePerson(it.name.get(), it.contact.asMap()) }
        val contributors = fmj.contributors.nullIfEmpty()?.map { SerializablePerson(it.name.get(), it.contact.asMap()) }
        val contact = fmj.contact.asMap()
        val license = fmj.license.nullIfEmpty()
        val icon = fmj.icon.orNull?.also {
            if (it is IconMap) {
                it.icons.keys.validateAll("^[1-9][0-9]*$", "Icon key")
            }
        }
        val custom = fmj.custom.nullIfEmpty()
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
