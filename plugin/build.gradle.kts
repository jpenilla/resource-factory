import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.indra)
    alias(libs.plugins.indra.publishing.gradle.plugin)
    alias(libs.plugins.indra.licenser.spotless)
}

group = "xyz.jpenilla"
version = "0.0.4"
description = "Gradle plugin for generating resources at build time"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(libs.configurateYaml)
    implementation(libs.configurateGson)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            apiVersion = "1.4"
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-opt-in=kotlin.io.path.ExperimentalPathApi")
        }
    }

    register("format") {
        group = "formatting"
        description = "Formats source code according to project style."
        dependsOn(spotlessApply)
    }
}

indra {
    apache2License()
    github("jpenilla", "resource-factory")
    publishSnapshotsTo("jmp", "https://repo.jpenilla.xyz/snapshots")
    configurePublications {
        pom {
            developers {
                developer {
                    id.set("jmp")
                    timezone.set("America/Phoenix")
                }
            }
        }
    }
    signWithKeyFromProperties("signingKey", "signingPassword")
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("../LICENSE_HEADER"))
}

spotless {
    val overrides = mapOf(
        "ktlint_standard_filename" to "disabled",
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_comment-wrapping" to "disabled", // allow block comments in between elements on the same line
    )
    kotlin {
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
    }
}

fun tags(vararg extra: String) = listOf("resource", "generator", "resource-generator") + extra

indraPluginPublishing {
    website("https://github.com/jpenilla/resource-factory")
    plugin(
        "resource-factory",
        "xyz.jpenilla.resourcefactory.ResourceFactoryPlugin",
        "Resource Factory",
        "Gradle plugin for generating resources at build time",
        tags()
    )
    plugin(
        "resource-factory-paper-convention",
        "xyz.jpenilla.resourcefactory.paper.PaperConvention",
        "Resource Factory Paper Convention",
        "Convention for xyz.jpenilla.resource-factory, registers a PaperPluginYml to the main source set and adds it as the paperPluginYml extension",
        tags("paper")
    )
    plugin(
        "resource-factory-bukkit-convention",
        "xyz.jpenilla.resourcefactory.bukkit.BukkitConvention",
        "Resource Factory Bukkit Convention",
        "Convention for xyz.jpenilla.resource-factory, registers a BukkitPluginYml to the main source set and adds it as the bukkitPluginYml extension",
        tags("bukkit")
    )
    plugin(
        "resource-factory-bungee-convention",
        "xyz.jpenilla.resourcefactory.bungee.BungeeConvention",
        "Resource Factory Bungee Convention",
        "Convention for xyz.jpenilla.resource-factory, registers a BungeePluginYml to the main source set and adds it as the bungeePluginYml extension",
        tags("bungee", "bungeecord")
    )
    plugin(
        "resource-factory-velocity-convention",
        "xyz.jpenilla.resourcefactory.velocity.VelocityConvention",
        "Resource Factory Velocity Convention",
        "Convention for xyz.jpenilla.resource-factory, registers a VelocityPluginJson to the main source set and adds it as the velocityPluginJson extension",
        tags("velocity")
    )
    plugin(
        "resource-factory-fabric-convention",
        "xyz.jpenilla.resourcefactory.fabric.FabricConvention",
        "Resource Factory Fabric Convention",
        "Convention for xyz.jpenilla.resource-factory, registers a FabricModJson to the main source set and adds it as the fabricModJson extension",
        tags("fabric")
    )
}
