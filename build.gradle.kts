plugins {
    val kotlinVersion: String by System.getProperties()
    val buildconfigVersion: String by System.getProperties()
    java
    `maven-publish`
    signing
    kotlin("jvm") version kotlinVersion
    id("com.github.gmazzo.buildconfig") version buildconfigVersion
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
    apply(plugin = "signing")
    apply(plugin = "com.github.gmazzo.buildconfig")

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

    val repositoryUrl =
        if (version.toString()
                .endsWith("SNAPSHOT")
        )
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        else
            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = project.group.toString()
                artifactId = project.name.toLowerCase()
                version = project.version.toString()

                pom {
                    name.set(project.name)
                    description.set("A compiler plugin to restrict API visibility from Kotlin sources.")
                    url.set("https://github.com/ZwenDo/${project.name}")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://mit-license.org/")
                        }
                    }
                    developers {
                        developer {
                            id.set("ZwenDo")
                            name.set("Lorris Creantor")
                            email.set("lorris.creantor@zwendo.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/ZwenDo/${project.name}.git")
                        developerConnection.set("scm:git:ssh://github.com/ZwenDo/${project.name}.git")
                        url.set("https://github.com/Zwendo/${project.name}.git")
                    }
                }
            }

            repositories {
                maven {
                    name = "MavenCentral"
                    setUrl(repositoryUrl)
                    credentials {
                        username = (project.properties["ossrhUsername"] as String?)!!
                        password = (project.properties["ossrhPassword"] as String?)!!
                    }
                }
            }

        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}

fun buildConfigSetup(vararg projects: Project) {
    projects.forEach {
        it.buildConfig {
            buildConfigField("String", "PLUGIN_ID", "\"${rootProject.name.toLowerCase()}\"")
            useKotlinOutput {
                internalVisibility = true
            }
        }
    }
}

buildConfigSetup(
    project(":restrikt-gradle-plugin"),
    project(":restrikt-compiler-plugin"),
)

