# Resource Factory Gradle Plugin

[![build](https://img.shields.io/github/actions/workflow/status/jpenilla/resource-factory/build.yml?branch=master)](https://github.com/jpenilla/resource-factory/actions)
[![license](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)
[![latest release](https://img.shields.io/gradle-plugin-portal/v/xyz.jpenilla.resource-factory)](https://plugins.gradle.org/plugin/xyz.jpenilla.resource-factory)

Gradle plugin for generating resource files at build time.

## Usage

1) Apply the plugin
    ```kotlin
    plugins {
      // Apply the plugin
      id("xyz.jpenilla.resource-factory") version "VERSION"
    }
    ```
2) Add resource factories to the desired source sets
    ```kotlin
    // for example, the 'main' source set
    sourceSets.main {
      resourceFactory {
        factories(/* ... */)
      }
    }
    ```

## Included factories

| Type               | Convention Plugin                                   |
|--------------------|-----------------------------------------------------|
| PaperPluginYml     | `xyz.jpenilla.resource-factory-paper-convention`    |
| BukkitPluginYml    | `xyz.jpenilla.resource-factory-bukkit-convention`   |
| VelocityPluginJson | `xyz.jpenilla.resource-factory-velocity-convention` |
| FabricModJson      | `xyz.jpenilla.resource-factory-fabric-convention`   |
| BungeePluginYml    | `xyz.jpenilla.resource-factory-bungee-convention`   |

The included factories can be used in two ways.
PaperPluginYml is used as an example, but the process is the same for the other included factories.

### Convention Plugins

The provided convention plugins can be applied in addition to or instead of the base `xyz.jpenilla.resource-factory`
plugin.
These conventions behave the same as the below manual examples, however they also register an extension for the resource
object.
This allows simplifying use to the following:

```kotlin
plugins {
    // Apply the convention plugin
    id("xyz.jpenilla.resource-factory-paper-convention") version "VERSION"
}

paperPluginYml {
    // Defaults for name, version, and description are inherited from the Gradle project
    main = "main.class.Name"
    authors.add("MyName")
    // configure fields...
}
```

### Manually

The included factories can be used manually in two ways.

1) Directly on the project instance, and then registered manually
    ```kotlin
    import xyz.jpenilla.resourcefactory.paper.paperPluginYml
    
    val yml = paperPluginYml {
      // Defaults for name, version, and description are inherited from the Gradle project
      main = "main.class.Name"
      authors.add("MyName")
      // configure fields...
    }

    sourceSets.main {
      resourceFactory {
        factory(yml.resourceFactory())
      }
    }
    ```
2) From within the resource factory extension
    ```kotlin
    sourceSets.main {
      resourceFactory {
        paperPluginYml {
          // Defaults for name, version, and description are inherited from the Gradle project
          main = "main.class.Name"
          authors.add("MyName")
          // configure fields...
        }
      }
    }
    ```
