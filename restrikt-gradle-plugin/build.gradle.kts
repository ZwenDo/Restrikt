plugins {
    id("java-gradle-plugin")
}

val kotlinVersion: String by System.getProperties()
val projectId: String by project
val projectDisplayName: String by project
val projectDescription: String by project
val projectImplementationClass: String by project
val projectGroup: String by project
val projectVersion: String by project

dependencies {
    compileOnly("org.jetbrains.kotlin", "kotlin-gradle-plugin-api", kotlinVersion)
}

gradlePlugin {
    plugins {
        create("restriktCompilerPlugin") {
            id = projectId
            displayName = projectDisplayName
            description = projectDescription
            implementationClass = projectImplementationClass
        }
    }
}

buildConfig {
    buildConfigField("String", "GROUP_ID", "\"$projectGroup\"")
    buildConfigField("String", "ANNOTATION_ID", "\"${project(":restrikt-annotation").name}\"")
    buildConfigField("String", "VERSION", "\"$projectVersion\"")
    buildConfigField("String", "EXTENSION_NAME", "\"${rootProject.name.toLowerCase()}\"")
    buildConfigField(
        "String",
        "COMPILER_PLUGIN_ID",
        "\"${project(":restrikt-compiler-plugin").name}\""
    )
}
