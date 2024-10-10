import java.util.Locale

plugins {
    id("java-gradle-plugin")
    val gradlePluginPublishVersion: String by System.getProperties()
    id("com.gradle.plugin-publish") version gradlePluginPublishVersion
}

val kotlinVersion: String by System.getProperties()
val projectGroup: String by project
val projectVersion: String by project

dependencies {
    compileOnly("org.jetbrains.kotlin", "kotlin-gradle-plugin-api", kotlinVersion)
}

gradlePlugin {
    vcsUrl = "https://github.com/ZwenDo/Restrikt.git"
    website = "https://github.com/ZwenDo/Restrikt"
    plugins {
        create("restriktCompilerPlugin") {
            id = "com.zwendo.restrikt2"
            displayName = "Restrikt 2.0 gradle plugin"
            description = "Gradle plugin for Restrikt 2.0 compiler plugin"
            implementationClass = "com.zwendo.restrikt2.gradle.RestriktGradlePlugin"
            tags = listOf(
                "kotlin", "k2", "java", "library", "jvm", "compiler-plugin", "hide-code", "library-development"
            )
        }
    }
}

buildConfig {
    buildConfigField("String", "VERSION", "\"$projectVersion\"")
    buildConfigField("String", "EXTENSION_NAME", "\"${rootProject.name}\"")
    buildConfigField("String", "GROUP_ID", "\"$projectGroup\"")
    buildConfigField(
        "String",
        "COMPILER_PLUGIN_ID",
        "\"${project(":restrikt2-compiler-plugin").name}\""
    )
}
