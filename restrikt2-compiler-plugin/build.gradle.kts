val kotlinVersion: String by System.getProperties()

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
}
