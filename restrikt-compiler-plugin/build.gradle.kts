val kotlinVersion: String by System.getProperties()
val kotlinMetadataJvmVersion: String by rootProject

dependencies {
    compileOnly("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-metadata-jvm", kotlinMetadataJvmVersion)
    implementation(project(":restrikt-annotation"))
}
