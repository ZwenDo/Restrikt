import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm") version "2.1.0"
    id("com.zwendo.restrikt2") version "0.2.0"
    id("com.github.gmazzo.buildconfig") version "5.5.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation(kotlin("reflect"))
}

restrikt2 {
    hideFromJavaAnnotations = setOf("com/zwendo/restrikt2/test/HFJ")
    hideFromKotlinAnnotations = setOf("com/zwendo/restrikt2/test/HFK")
    packagePrivateAnnotations = setOf("com/zwendo/restrikt2/test/PP")
}


tasks {
    test {
        useJUnitPlatform()
    }
}
