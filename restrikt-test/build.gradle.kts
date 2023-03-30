plugins {
    java
    kotlin("jvm") version "1.7.10"
    id("com.zwendo.restrikt") version "4.0.0"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
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

val kotlinDefaultReason = "this is a default message for kotlin"
val deprecatedDefaultReason = "this element is hidden due to HideFromKotlin"
val javaDefaultReason = "not for java"
val packagePrivateReason = "this implementation must be package private"

restrikt {

    hideFromKotlin {
        defaultReason = kotlinDefaultReason
        deprecatedMessage = deprecatedDefaultReason
    }

    hideFromJava {
        defaultReason = javaDefaultReason
    }

    packagePrivate {
        defaultReason = packagePrivateReason
    }

}

buildConfig {
    buildConfigField("String", "KOTLIN_DEFAULT_REASON", "\"$kotlinDefaultReason\"")
    buildConfigField("String", "JAVA_DEFAULT_REASON", "\"$javaDefaultReason\"")
    buildConfigField("String", "PACKAGE_PRIVATE_REASON", "\"$packagePrivateReason\"")
    buildConfigField("String", "DEPRECATED_REASON", "\"$deprecatedDefaultReason\"")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
