pluginManagement {
    includeBuild("plugin")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "resource-factory-parent"

include("tester")
