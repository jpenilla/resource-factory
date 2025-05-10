package xyz.jpenilla.resourcefactory.neoforge

import io.leangen.geantyref.TypeToken
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.util.NamingSchemes
import xyz.jpenilla.resourcefactory.ConfigurateSingleFileResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactory
import xyz.jpenilla.resourcefactory.ResourceFactoryExtension
import xyz.jpenilla.resourcefactory.util.ConfigurateCustomValueProviderSerializer
import xyz.jpenilla.resourcefactory.util.CustomValueFactory
import xyz.jpenilla.resourcefactory.util.CustomValueProvider
import xyz.jpenilla.resourcefactory.util.Pattern
import xyz.jpenilla.resourcefactory.util.ProjectMetaConventions
import xyz.jpenilla.resourcefactory.util.nullAction
import xyz.jpenilla.resourcefactory.util.nullIfEmpty
import xyz.jpenilla.resourcefactory.util.orNullValidating
import xyz.jpenilla.resourcefactory.util.validate
import javax.inject.Inject

/**
 * Create a [NeoForgeModsToml] and configure it with the given [configure] block.
 *
 * @param configure the block to configure the [NeoForgeModsToml] with
 * @return the created and configured [NeoForgeModsToml]
 */
fun Project.neoForgeModsToml(configure: Action<NeoForgeModsToml> = nullAction()): NeoForgeModsToml {
    val toml = objects.newInstance<NeoForgeModsToml>(this)
    configure.execute(toml)
    return toml
}

/**
 * A `neoforge.mods.toml` configuration.
 *
 * See [the official docs](https://docs.neoforged.net/docs/gettingstarted/modfiles#neoforgemodstoml) for more information.
 *
 * @see [neoForgeModsToml]
 * @see [ResourceFactoryExtension.neoForgeModsToml]
 */
abstract class NeoForgeModsToml @Inject constructor(
    @Transient
    private val objects: ObjectFactory,
    @Transient
    private val project: Project,
) : ConfigurateSingleFileResourceFactory.Simple.ValueProvider, ResourceFactory.Provider, CustomValueFactory() {

    companion object {
        private const val MOD_ID_PATTERN: String = "^[a-z0-9_]{2,64}$"
        private const val NAMESPACE_PATTERN: String = "^[a-z0-9_.-]{2,64}$"
        private const val FILE_NAME: String = "META-INF/neoforge.mods.toml"
    }

    @get:Input
    val modLoader: Property<String> = objects.property<String>().convention("javafml")

    @get:Input
    val loaderVersion: Property<String> = objects.property<String>().convention("[1,)")

    @get:Input
    val license: Property<String> = objects.property()

    fun apache2License() = license.set("Apache-2.0")

    fun mitLicense() = license.set("MIT")

    @get:Input
    @get:Optional
    val showAsResourcePack: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val showAsDataPack: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val services: ListProperty<String> = objects.listProperty()

    @get:Input
    @get:Optional
    val issueTrackerUrl: Property<String> = objects.property()

    @get:Nested
    val mods: NamedDomainObjectContainer<Mod> = objects.domainObjectContainer(Mod::class)

    /**
     * Register or configure a [Mod] with the given [id].
     *
     * @param id the id of the mod
     * @param configure the block to configure the [Mod] with
     */
    @JvmOverloads
    fun mod(id: String, configure: Action<Mod> = nullAction()): NamedDomainObjectProvider<Mod> {
        return if (id in mods.names) {
            mods.named(id, configure)
        } else {
            mods.register(id, configure)
        }
    }

    /**
     * Register or configure the convention [Mod].
     *
     * If the mod id is omitted, it will be derived from the project name, lowercased and with disallowed characters
     * replaced by `_`.
     *
     * [Mod.setConventionsFromProjectMeta] will be called with the project before the [configure] block is executed.
     *
     * @param id the id of the mod, or null to derive from the project name
     * @param configure the block to configure the [Mod] with
     */
    @JvmOverloads
    fun conventionMod(id: String? = null, configure: Action<Mod> = nullAction()): NamedDomainObjectProvider<Mod> {
        val modId = id ?: project.name.lowercase().replace(Regex("[- .]"), "_")
        return mod(modId) {
            setConventionsFromProjectMeta(project)
            configure.execute(this)
        }
    }

    open class Mod @Inject constructor(
        private val name: String,
        @Transient
        private val objects: ObjectFactory
    ) : Named, ProjectMetaConventions {
        init {
            name.validate(MOD_ID_PATTERN, "NeoForge mod id")
        }

        /**
         * Returns the [modId].
         *
         * @return [modId]
         */
        @Internal
        override fun getName(): String {
            return name
        }

        @get:Input
        val modId: String
            get() = name

        @get:Input
        @get:Optional
        @Pattern(NAMESPACE_PATTERN, "NeoForge mod namespace")
        val namespace: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val version: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val displayName: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val description: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val logoFile: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val logoBlur: Property<Boolean> = objects.property()

        @get:Input
        @get:Optional
        val updateJsonUrl: Property<String> = objects.property()

        @get:Input
        val features: MapProperty<String, String> = objects.mapProperty()

        @get:Nested
        val modProperties: MapProperty<String, CustomValueProvider<*>> = objects.mapProperty()

        /**
         * Custom values merged into the root TOML table for this mod.
         */
        @get:Nested
        val custom: MapProperty<String, CustomValueProvider<*>> = objects.mapProperty()

        @get:Input
        @get:Optional
        val modUrl: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val credits: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val authors: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val displayUrl: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val enumExtensions: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val featureFlags: Property<String> = objects.property()

        @get:Nested
        val dependencies: Dependencies = Dependencies(objects)

        fun dependencies(op: Action<Dependencies>) {
            op.execute(dependencies)
        }

        class Dependencies(private val objects: ObjectFactory) {
            @get:Nested
            val dependencies: ListProperty<Dependency> = objects.listProperty()

            fun add(modId: String, configure: Action<Dependency>): Dependency {
                val dep = Dependency(objects)
                dep.modId.set(modId)
                dependencies.add(dep)
                configure.execute(dep)
                return dep
            }

            @JvmOverloads
            fun required(modId: String, versionRange: String? = null, configure: Action<Dependency> = nullAction()): Dependency = add(modId) {
                type.set(DependencyType.REQUIRED)
                versionRange?.let { this.versionRange.set(it) }
                configure.execute(this)
            }

            @JvmOverloads
            fun optional(modId: String, versionRange: String? = null, configure: Action<Dependency> = nullAction()): Dependency = add(modId) {
                type.set(DependencyType.OPTIONAL)
                versionRange?.let { this.versionRange.set(it) }
                configure.execute(this)
            }

            @JvmOverloads
            fun incompatible(modId: String, versionRange: String? = null, configure: Action<Dependency> = nullAction()): Dependency = add(modId) {
                type.set(DependencyType.INCOMPATIBLE)
                versionRange?.let { this.versionRange.set(it) }
                configure.execute(this)
            }

            @JvmOverloads
            fun discouraged(modId: String, versionRange: String? = null, configure: Action<Dependency> = nullAction()): Dependency = add(modId) {
                type.set(DependencyType.DISCOURAGED)
                versionRange?.let { this.versionRange.set(it) }
                configure.execute(this)
            }
        }

        override fun setConventionsFromProjectMeta(project: Project) {
            displayName.convention(project.name)
            version.convention(project.version.toString())
            description.convention(project.description)
        }
    }

    open class Dependency(
        @Transient
        private val objects: ObjectFactory
    ) {
        @get:Input
        @Pattern(MOD_ID_PATTERN, "NeoForge mod id")
        val modId: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val type: Property<DependencyType> = objects.property()

        @get:Input
        @get:Optional
        val reason: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val versionRange: Property<String> = objects.property()

        @get:Input
        @get:Optional
        val ordering: Property<DependencyOrdering> = objects.property()

        fun before() {
            ordering.set(DependencyOrdering.BEFORE)
        }

        fun after() {
            ordering.set(DependencyOrdering.AFTER)
        }

        @get:Input
        @get:Optional
        val side: Property<DependencySide> = objects.property()

        fun client() {
            side.set(DependencySide.CLIENT)
        }

        fun server() {
            side.set(DependencySide.SERVER)
        }

        @get:Input
        @get:Optional
        val referralUrl: Property<String> = objects.property()
    }

    enum class DependencyType {
        REQUIRED,
        OPTIONAL,
        INCOMPATIBLE,
        DISCOURAGED,
    }

    enum class DependencyOrdering {
        BEFORE,
        AFTER,
        NONE,
    }

    enum class DependencySide {
        CLIENT,
        SERVER,
        BOTH,
    }

    @get:Nested
    val accessTransformers: ListProperty<AccessTransformer> = objects.listProperty()

    fun accessTransformer(file: String): AccessTransformer {
        val at = AccessTransformer(objects)
        at.file.set(file)
        accessTransformers.add(at)
        return at
    }

    fun accessTransformers(vararg files: String) {
        for (file in files) {
            accessTransformer(file)
        }
    }

    open class AccessTransformer(
        @Transient
        private val objects: ObjectFactory
    ) {
        @get:Input
        val file: Property<String> = objects.property()
    }

    @get:Nested
    val mixins: ListProperty<Mixin> = objects.listProperty()

    fun mixin(config: String): Mixin {
        val mixin = Mixin(objects)
        mixin.config.set(config)
        mixins.add(mixin)
        return mixin
    }

    fun mixins(vararg configs: String) {
        for (config in configs) {
            mixin(config)
        }
    }

    open class Mixin(
        @Transient
        private val objects: ObjectFactory
    ) {
        @get:Input
        val config: Property<String> = objects.property()
    }

    /**
     * Custom values merged into the root TOML table for the mods toml.
     */
    @get:Nested
    val custom: MapProperty<String, CustomValueProvider<*>> = objects.mapProperty()

    override fun resourceFactory(): ResourceFactory {
        val gen = objects.newInstance(ConfigurateSingleFileResourceFactory.Simple::class)
        gen.toml {
            defaultOptions {
                it.serializers { s ->
                    s.registerAnnotatedObjects(
                        ObjectMapper.factoryBuilder()
                            .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
                            .build()
                    )
                        .register(object : TypeToken<CustomValueProvider<*>>() {}, ConfigurateCustomValueProviderSerializer)
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
    open class Serializable(modsToml: NeoForgeModsToml) {
        @Setting(nodeFromParent = true)
        val custom: Map<String, CustomValueProvider<*>>? = modsToml.custom.nullIfEmpty()

        val modLoader: String = modsToml.modLoader.get()
        val loaderVersion: String = modsToml.loaderVersion.get()
        val license: String = modsToml.license.get()
        val showAsResourcePack: Boolean? = modsToml.showAsResourcePack.orNull
        val showAsDataPack: Boolean? = modsToml.showAsDataPack.orNull
        val services: List<String>? = modsToml.services.nullIfEmpty()
        val issueTrackerURL: String? = modsToml.issueTrackerUrl.orNull
        val mods: List<SerializableMod>? = modsToml.mods.nullIfEmpty()?.values?.map { SerializableMod(it) }
        val accessTransformers: List<SerializableAccessTransformer>? = modsToml.accessTransformers.nullIfEmpty()?.map { SerializableAccessTransformer(it) }
        val mixins: List<SerializableMixin>? = modsToml.mixins.nullIfEmpty()?.map { SerializableMixin(it) }
        val dependencies: Map<String, List<SerializableDependency>>? = modsToml.mods.nullIfEmpty()?.mapValues { (_, mod) ->
            mod.dependencies.dependencies.get().map { SerializableDependency(it) }
        }
    }

    @ConfigSerializable
    open class SerializableMod(mod: Mod) {
        @Setting(nodeFromParent = true)
        val custom: Map<String, CustomValueProvider<*>>? = mod.custom.nullIfEmpty()

        val modId: String = mod.modId
        val namespace: String? = mod::namespace.orNullValidating()
        val version: String? = mod.version.orNull
        val displayName: String? = mod.displayName.orNull
        val description: String? = mod.description.orNull
        val logoFile: String? = mod.logoFile.orNull
        val logoBlur: Boolean? = mod.logoBlur.orNull
        val updateJSONURL: String? = mod.updateJsonUrl.orNull
        val features: Map<String, String>? = mod.features.nullIfEmpty()
        val modproperties: Map<String, CustomValueProvider<*>>? = mod.modProperties.nullIfEmpty()
        val modUrl: String? = mod.modUrl.orNull
        val credits: String? = mod.credits.orNull
        val authors: String? = mod.authors.orNull
        val displayURL: String? = mod.displayUrl.orNull
        val enumExtensions: String? = mod.enumExtensions.orNull
        val featureFlags: String? = mod.featureFlags.orNull
    }

    @ConfigSerializable
    open class SerializableAccessTransformer(at: AccessTransformer) {
        val file: String = at.file.get()
    }

    @ConfigSerializable
    open class SerializableMixin(mixin: Mixin) {
        val config: String = mixin.config.get()
    }

    @ConfigSerializable
    open class SerializableDependency(dep: Dependency) {
        val modId: String = dep.modId.get()
        val type: String? = dep.type.orNull?.name?.lowercase()
        val reason: String? = dep.reason.orNull
        val versionRange: String? = dep.versionRange.orNull
        val ordering: DependencyOrdering? = dep.ordering.orNull
        val side: DependencySide? = dep.side.orNull
        val referralUrl: String? = dep.referralUrl.orNull
    }
}
