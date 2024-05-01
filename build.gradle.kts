import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.TaskTriggersConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("fabric-loom")
    `maven-publish`
    eclipse
    id("org.jetbrains.gradle.plugin.idea-ext")
}

val mavenVersion = System.getenv("CI_COMMIT_TAG") ?: System.getenv("CI_COMMIT_SHORT_SHA")?.let { "$it-dev" } ?: "0.0.0-SNAPSHOT"
val modId: String by project
val modName: String by project

val mavenGroup: String by project
val mavenArtifact: String by project

val minecraftVersion = project.ext["minecraft.version"] as String
val yarnMappings = project.ext["yarn.version"] as String
val fabricLoaderVersion = project.ext["fabric.loader.version"] as String

val fabricApiVersion = project.ext["fabric.api.version"] as String
val fabricKotlinVersion = project.ext["fabric.kotlin.version"] as String
val serializationVersion = project.ext["kotlinx.serialization.version"] as String
val configlibVersion = project.ext["configlib.version"] as String

version = mavenVersion
group = mavenGroup
project.base.archivesName.set(mavenArtifact)

repositories {
    maven {
        name = "Gigaherz Graph"
        url = uri("https://dogforce-games.com/maven")
    }
    maven {
        name = "Modmaven"
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
        }
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    include(modApi("teamreborn:energy:2.3.0") {
        isTransitive = false
    })
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.3.5")!!)!!)

    modCompileOnlyApi("appeng:appliedenergistics2-fabric:15.1.0:api") {
        exclude("net.fabricmc.fabric-api:fabric-api")
    }

    implementation(include("dev.gigaherz.graph:GraphLib3:3.0.4")!!)
}

val targetJavaVersion = 17
val templateSrc = project.rootDir.resolve("src/main/templates")
val templateDest = project.buildDir.resolve("generated/templates")
val datagenDest = project.rootDir.resolve("src/main/generated")
val templateProps = mapOf(
    "modVersion" to project.version as String,
    "modId" to modId,
    "modName" to modName,
    "minecraftVersion" to minecraftVersion,
    "fabricLoaderVersion" to fabricLoaderVersion,
    "configlibVersion" to configlibVersion,
    "fabricKotlinVersion" to fabricKotlinVersion,
)

loom {
    accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
    runs {
        register("datagenClient") {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=$datagenDest")
            vmArg("-Dfabric-api.datagen.modid=$modId")

            runDir("build/datagen")
        }
    }
}

fabricApi {
    configureDataGeneration()
}

tasks {
    withType<ProcessResources> {
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        inputs.properties(templateProps)
        filesMatching("fabric.mod.json") {
            expand(templateProps)
        }
    }
    create<Copy>("generateTemplates") {
        filteringCharset = "UTF-8"

        inputs.properties(templateProps)
        from(templateSrc)
        expand(templateProps)
        into(templateDest)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }

        dependsOn("generateTemplates", "processResources")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = targetJavaVersion.toString()

        dependsOn("generateTemplates", "processResources")
    }
    withType<Jar> {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }

        archiveBaseName.set(mavenArtifact)

        dependsOn("generateTemplates", "processResources")
    }
}


java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    withSourcesJar()
}

sourceSets {
    main {
        java {
            srcDir(templateDest)
        }
        resources {
            srcDir(datagenDest)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            version = project.version as String
            artifactId = mavenArtifact
            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("CI_JOB_TOKEN") != null) {
            maven {
                name = "GitLab"
                val projectId = System.getenv("CI_PROJECT_ID")
                val apiV4 = System.getenv("CI_API_V4_URL")
                url = uri("$apiV4/projects/$projectId/packages/maven")
                authentication {
                    create("token", HttpHeaderAuthentication::class.java) {
                        credentials(HttpHeaderCredentials::class.java) {
                            name = "Job-Token"
                            value = System.getenv("CI_JOB_TOKEN")
                        }
                    }
                }
            }
        }
    }
}

rootProject.idea.project {
    this as ExtensionAware
    configure<ProjectSettings> {
        this as ExtensionAware
        configure<TaskTriggersConfig> {
            afterSync(tasks["generateTemplates"], tasks["processResources"])
        }
    }
}

rootProject.eclipse.synchronizationTasks("generateTemplates", "processResources")
