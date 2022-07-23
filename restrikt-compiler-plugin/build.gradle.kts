val kotlinVersion: String by System.getProperties()

dependencies {
    compileOnly("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    implementation(project(":restrikt-annotations"))
}
