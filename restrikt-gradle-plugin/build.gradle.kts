plugins {
    id("java-gradle-plugin")
}

val kotlinVersion: String by System.getProperties()
val projectId: String by project
val projectDisplayName: String by project
val projectDescription: String by project
val projectImplementationClass: String by project

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
