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
val javaVersion = JavaVersion.toVersion(jvmVersion)

subprojects {
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

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
        withJavadocJar()
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
        }
    }

    tasks {
        test {
            useJUnitPlatform()
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
                artifactId = project.name
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
                        username = (project.properties["ossrhUsername"] as? String) ?: throw AssertionError("ossrhUsername is missing or invalid")
                        password = (project.properties["ossrhPassword"] as? String) ?: throw AssertionError("ossrhPassword is missing or invalid")
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }

}

val pluginEnabled: String by project
val toplevelPrivateConstructor: String by project
val automaticInternalHiding: String by project
val annotationProcessing: String by project
val hideFromJava: String by project
val hideFromKotlin: String by project
val packagePrivate: String by project
val removeDefaultAnnotations: String by project

fun buildConfigGenericSetup(vararg projects: Project) {
    projects.forEach {
        it.buildConfig {
            buildConfigField("String", "PLUGIN_ID", "\"$projectGroup.${rootProject.name}\"")
            buildConfigField("String", "ENABLED", "\"$pluginEnabled\"")
            buildConfigField("String", "TOPLEVEL_PRIVATE_CONSTRUCTOR", "\"$toplevelPrivateConstructor\"")
            buildConfigField("String", "AUTOMATIC_INTERNAL_HIDING", "\"$automaticInternalHiding\"")
            buildConfigField("String", "ANNOTATION_PROCESSING", "\"$annotationProcessing\"")
            buildConfigField("String", "HIDE_FROM_JAVA_ANNOTATION", "\"$hideFromJava\"")
            buildConfigField("String", "HIDE_FROM_KOTLIN_ANNOTATION", "\"$hideFromKotlin\"")
            buildConfigField("String", "PACKAGE_PRIVATE_ANNOTATION", "\"$packagePrivate\"")
            buildConfigField("String", "IGNORE_DEFAULT_ANNOTATIONS", "\"$removeDefaultAnnotations\"")

            useKotlinOutput {
                internalVisibility = true
            }
        }
    }
}

buildConfigGenericSetup(
    project(":restrikt2-gradle-plugin"),
    project(":restrikt2-compiler-plugin"),
)
