# Restrikt

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.zwendo.restrikt?color=%2366dcb8&logo=gradle)](https://plugins.gradle.org/plugin/com.zwendo.restrikt)
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt-annotation)](https://search.maven.org/artifact/com.zwendo/restrikt-annotation)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.10-7f52ff.svg?logo=kotlin)](https://kotlinlang.org)

**A Kotlin/JVM compiler plugin to hide elements from Kotlin sources.**

## Summary

1. [Dependency](#dependency)
   1. [Gradle plugins DSL](#using-the-gradle-plugin-dsl-gradle-21)
   2. [apply method](#using-apply-method-gradle-prior-to-21) 
2. [Usage](#usage)
   1. [Plugin Configuration](#plugin-configuration)
   2. [Annotation](#annotation-usage)
   3. [**Important notes**](#important-notes)
3. [How it works](#how-it-works)

## Dependency

### Using the Gradle plugin DSL (Gradle 2.1+)

Using Kotlin DSL:

```kotlin
plugins {
   id("com.zwendo.restrikt") version "[latest-version]"
}
```

Using Groovy DSL:

```groovy
plugins {
   id 'com.zwendo.restrikt' version '[latest-version]'
}
```

### Using `apply` method (Gradle prior to 2.1)

Using Kotlin DSL:

```kotlin
buildscript {
   repositories {
      maven {
         url = uri("https://plugins.gradle.org/m2/")
      }
   }

   dependencies {
      classpath("gradle.plugin.com.restrikt:restrikt:[latest-version]")
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
      classpath 'gradle.plugin.com.restrikt:restrikt:[latest-version]'
   }
}

apply plugin: 'com.zwendo.restrikt'
```

## Usage

### Plugin Configuration

You can configure the plugin using the configuration DSL.

```kotlin
restrkt {
    enabled = true
    // ...
}
```

Currently supported configuration options:

- `enabled`: `true` or `false` (default: `true`). Whether the plugin elements hiding is enabled or not, allows to
  generate classes without hiding the elements.
- `keepAnnotations`: `true` or `false` (default: `true`). Whether the annotations of the plugin should be kept in the
  classfile.

### Annotation usage
Once you have added the plugin in your `build.gradle` file, it will automatically add the dependency to the restrikt
annotation(s) artifact corresponding to your version of the plugin.

It will allow you to use the `@RestrictedToJava` annotation. This annotation is designed to be an equivalent of the
Kotlin [@JvmSynthetic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-synthetic/) annotation for
Java-Specific target hiding. 

To hide elements from Kotlin, simply add the `@RestrictedToJava` annotation on it. It can be applied to any **class**,
**method**, or **property**. 

Example:
```kotlin
@RestrictedToJava
fun foo() { // will be only visible in Java
    // ...
}
```

The annotation also takes an optional parameter representing the reason for the restriction:

```kotlin
@RestrictedToJava("This class is designed for Java")
class Bar { // will be only visible in Java
    // ...
}
```

### Important notes

Annotated elements will still be accessible:

- at **compile time** from **Kotlin sources** in the same **package**
- at **runtime** from **everywhere**, meaning that already compiled code will still be able to access it

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

**NOTE:** The message of the `RestrictedToJava` annotation will be transferred to the `Deprecated` annotation.

Generating the `Deprecated` annotation or simply using it directly have slightly different outcomes. Indeed, the
`Deprecated` annotation (with `HIDDEN` level) acts as a flag for the Kotlin compiler. The latter will add the JVM
`ACC_SYNTHETIC` flag for the element in the produced classfile, making it also invisible for Java sources. The hack is
that the Kotlin compiler runs before calling the compiler plugin, so when it writes the classfile, the `Deprecated`
annotation is not present meaning that the `ACC_SYNTHETIC` flag is not set.
