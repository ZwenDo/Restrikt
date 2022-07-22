plugins {
    val kotlinVersion: String by System.getProperties()
    java
    `maven-publish`
    kotlin("jvm") version kotlinVersion
}

val projectGroup: String by project
val projectVersion: String by project
val jvmVersion: String by project
val junitVersion: String by project
val javaVersion = JavaVersion.VERSION_1_8

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    group = projectGroup
    version = projectVersion

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
        withJavadocJar()
    }

    tasks {
        test {
            useJUnitPlatform()
        }


        compileKotlin {
            kotlinOptions {
                jvmTarget = jvmVersion
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])
                groupId = projectGroup
                artifactId = project.name.toLowerCase()
                version = projectVersion
                pom {
                    name.set(project.name)

                    developers {
                        developer {
                            id.set("ZwenDo")
                        }
                    }
                }
            }
        }
    }
}
