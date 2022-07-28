# Restrikt

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.zwendo.restrikt?color=%2366dcb8&logo=gradle)](https://plugins.gradle.org/plugin/com.zwendo.restrikt)
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt-annotation)](https://search.maven.org/artifact/com.zwendo/restrikt-annotation)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.10-7f52ff.svg?logo=kotlin)](https://kotlinlang.org)

**A Kotlin/JVM compiler plugin to hide elements from Kotlin sources.**

## Summary

1. [Dependency](#dependency)
2. [Usage](#usage)
3. [How it works](#how-it-works)

## Dependency

### Using the Gradle plugin DSL (Gradle 2.1+)

Using Kotlin DSL:

```kotlin
plugins {
    id("com.zwendo.restrikt") version "[lastest-version]"
}
```

Using Groovy DSL:

```groovy
plugins {
    id 'com.zwendo.restrikt' version '[lastest-version]'
}
```

### Using `apply` method (Gradle prior to 2.1)

Using Kotlin DSL:

```groovy
buildscript {
    repositories {
        maven {
            url("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath("gradle.plugin.com.restrikt:restrikt:[lastest-version]")
    }
}

apply(plugin = "com.zwendo.restrikt")
```

Using Groovy DSL:

```groovy
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
    }

    dependencies {
        classpath 'gradle.plugin.com.restrikt:restrikt:[lastest-version]'
    }
}

apply plugin: 'com.zwendo.restrikt'
```

## Usage

To hide elements from Kotlin, simply add the `@RestrictedToJava` annotation. It can be applied to any class, method, or
field. The element will still be accessible at runtime, meaning that compiled code will still be able to access it.

```kotlin
@RestrictedToJava
fun foo() { // will be only visible in Java
    // ...
}
```

The annotation takes an optional parameter representing the reason for the restriction.

```kotlin
@RestrictedToJava("This method is designed for Java")
class Bar { // will be only visible in Java
    // ...
}
```

## How it works

To effectively hide elements from Kotlin, the plugin generates on the marked elements the
[@Deprecated](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecated/) annotation. This annotation used with
the [DeprecationLevel.HIDDEN](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecation-level/-h-i-d-d-e-n.html)
level, makes the element invisible to Kotlin sources, but still visible to Java sources.

```kotlin
// Foo.kt
@RestrictedToJava("java only")
class Foo {
    // ...
}

// will be compiled to

// Foo.class
@RestrictedToJava
@Deprecated("java only", DeprecationLevel.HIDDEN)
class Foo {
    // ...
}
```

Generating the `Deprecated` annotation or simply using it directly have slightly different outcomes. Indeed, the
`Deprecated` annotation acts as a flag for the Kotlin compiler. The latter will add the JVM `ACC_SYNTHETIC` flag for the
element in the produced classfile, making it also invisible for Java sources. The hack is that the Kotlin compiler runs
before calling the compiler plugin, so when it writes the classfile, the `Deprecated` annotation is not present meaning
that the `ACC_SYNTHETIC` flag is not set.
