plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.0.0"
}

val kotlinVersion: String by System.getProperties()
val projectGroup: String by project
val projectVersion: String by project

dependencies {
    compileOnly("org.jetbrains.kotlin", "kotlin-gradle-plugin-api", kotlinVersion)
}

pluginBundle {
    website = "https://github.com/ZwenDo/Restrikt"
    vcsUrl = "https://github.com/ZwenDo/Restrikt.git"
    tags = listOf("kotlin", "java", "library", "jvm", "compiler-plugin", "hide-code", "library-development")
}

gradlePlugin {
    plugins {
        create("restriktCompilerPlugin") {
            id = "com.zwendo.restrikt"
            displayName = "Restrikt gradle plugin"
            description = "Gradle plugin for Restrikt compiler plugin"
            implementationClass = "com.zwendo.restrikt.gradle.GradlePlugin"
        }
    }
}

buildConfig {
    buildConfigField("String", "GROUP_ID", "\"$projectGroup\"")
    buildConfigField("String", "VERSION", "\"$projectVersion\"")
    buildConfigField("String", "EXTENSION_NAME", "\"${rootProject.name.toLowerCase()}\"")
    buildConfigField("String", "COMPILER_PLUGIN_ID", "\"${project(":restrikt-compiler-plugin").name}\"")
}
