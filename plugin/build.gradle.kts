import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.indra)
    alias(libs.plugins.indra.publishing.gradle.plugin)
    alias(libs.plugins.indra.licenser.spotless)
}

group = "xyz.jpenilla"
version = "0.0.3-SNAPSHOT"
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

indraPluginPublishing {
    website("https://github.com/jpenilla/resource-factory")
    plugin(
        "resource-factory",
        "xyz.jpenilla.resourcefactory.ResourceFactoryPlugin",
        "Resource Factory",
        "Gradle plugin for generating resources at build time",
        listOf("resource", "generator", "resource-generator")
    )
}
