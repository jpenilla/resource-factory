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
      id("xyz.jpenilla.resource-factory") version "0.0.1"
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

- PaperPluginYml
- BukkitPluginYml

Bukkit and Paper plugin YMLs can be created in two ways:
1) Directly on the project instance, and then registered manually
    ```kotlin
    import xyz.jpenilla.resourcefactory.(bukkit|paper).(bukkit|paper)PluginYml
    
    val yml = (bukkit|paper)PluginYml {
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
        (bukkit|paper)PluginYml {
          main = "main.class.Name"
          authors.add("MyName")
          // configure fields...
        }
      }
    }
    ```
