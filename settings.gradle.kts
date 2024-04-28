pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version extra["kotlin.version"] as String
        kotlin("plugin.serialization") version extra["kotlin.version"] as String
        id("fabric-loom") version extra["fabric.loom.version"] as String
        id("org.jetbrains.gradle.plugin.idea-ext") version extra["idea-ext.version"] as String
    }
}
