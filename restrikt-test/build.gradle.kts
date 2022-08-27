plugins {
    java
    kotlin("jvm") version "1.7.10"
    id("com.zwendo.restrikt") version "2.0.0"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation(kotlin("reflect"))
    implementation("com.zwendo", "restrikt-annotation", "2.0.0")
}

val kotlinDefaultReason = "this is a default message for kotlin"
val javaDefaultReason = "not for java"

restrikt {

    hideFromKotlin {
        defaultReason = kotlinDefaultReason
    }

    hideFromJava {
        defaultReason = javaDefaultReason
    }

}


buildConfig {
    buildConfigField("String", "KOTLIN_DEFAULT_REASON", "\"$kotlinDefaultReason\"")
    buildConfigField("String", "JAVA_DEFAULT_REASON", "\"$javaDefaultReason\"")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
