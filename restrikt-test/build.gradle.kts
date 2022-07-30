plugins {
    java
    kotlin("jvm") version "1.7.10"
    id("com.zwendo.restrikt") version "1.+"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation(kotlin("reflect"))
}

val restriktDefaultMessage = "this is a default message"

restrikt {
    defaultReason = restriktDefaultMessage
}


buildConfig {
    buildConfigField("String", "DEFAULT_REASON", "\"$restriktDefaultMessage\"")
    useKotlinOutput {
        internalVisibility = true
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
