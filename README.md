# Restrikt

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.zwendo.restrikt?color=%2366dcb8&logo=gradle)](https://plugins.gradle.org/plugin/com.zwendo.restrikt)
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt-annotation)](https://search.maven.org/artifact/com.zwendo/restrikt-annotation)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.10-7f52ff.svg?logo=kotlin)](https://kotlinlang.org)

**A Kotlin/JVM compiler plugin to easily restrict symbol access, from external Kotlin and Java sources.**

This plugin offers two ways to hide symbols:

- An **automatic way**, with the automatic hiding of ``internal`` symbols ;
- A **manual way**, by using two different annotations to hide symbols from either Kotlin or Java sources.

## Summary

1. [Dependency](#dependency)
   1. [Gradle plugins DSL](#using-the-gradle-plugin-dsl-gradle-21)
   2. [apply method](#using-apply-method-gradle-prior-to-21)
2. [Usage](#usage)
   1. [Plugin Configuration](#plugin-configuration)
   2. [Internal symbols hiding](#internal-symbols-hiding)
   3. [Annotations](#annotations-usage)
   4. [**Important notes**](#important-notes)
3. [Known issues](#known-issues)
4. [How it works](#how-it-works)
5. [Future plans](#future-plans)

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
   option = value
   // ...
}
```

Here are the currently supported default configuration options:

|          name           |  type   |               default                | description                                                               |
|:-----------------------:|:-------:|:------------------------------------:|---------------------------------------------------------------------------|
| automaticInternalHiding | boolean |                `true`                | Whether the internal symbols should be automatically hidden.              |
|  annotationProcessing   | boolean |                `true`                | Whether the plugin annotations should be parsed to manually hide symbols. |

Moreover, both annotations of the plugin can be individually configured using their own DSL (``hideFromKotlin`` or
``hideFromJava``), with the following configuration options:

|      name      |  type   | default | description                                                                                                       |
|:--------------:|:-------:|:-------:|-------------------------------------------------------------------------------------------------------------------|
|    enabled     | boolean | `true`  | Whether the annotation should be processed to hide symbols. (works only if ``annotationProcessing`` is ``true``). |
| keepAnnotation | boolean | `true`  | Whether the annotation should be written to the classfile.                                                        |
| defaultReason  | string  | `none`  | The default reason written on the annotation if no specific reason is provided.                                   |

### Internal symbols hiding

Restrikt plugin features automatic hiding of internal symbols in Kotlin sources. At compile time, all symbols with the
``internal`` visibility automatically receives the JVM ``ACC_SYNTHETIC`` flag, making them invisible to Java sources.

### Annotations usage

This plugin provides two annotations intended for symbol access restrictions. These two annotations, namely
``HideFromJava`` and ``HideFromKotlin``, are used to hide symbols from Java and Kotlin sources respectively. They are
designed to be used in the same way, just by placing the right annotation on the symbol to hide as follows:

```kotlin
@HideFromJava
fun someFunction() { // will be hidden from java sources
   // ...
}

@HideFromKotlin
class SomeClass // will be hidden from kotlin sources
```

Both annotations also accepts a string parameter to indicate the reason of the restriction. If no message is provided,
the default message defined in the plugin configuration will be used instead.

```kotlin
@HideFromKotlin("This class is designed for Java")
class Bar { // will be hidden from kotlin sources
   // ...
}
```

### Important notes

- All hidden elements will still be accessible at runtime, meaning that already compiled code will still be able to
  access it ;
- Symbols hidden from Kotlin will still be accessible at compile-time from Kotlin sources in the same package.

### Known issues

#### **from 2.0.0**

With the new way of modifying the compilation, the plugin will run into an error if called to compile a project that
meets all the following conditions:

- Define an inline function that takes a ``crossinline`` functional type and returns an anonymous object. The lambda
  must be captured by the anonymous object ;
- Define a function calling the previous function with a literal lambda.

These conditions can be illustrated by the following example:

```kotlin
// 1
inline fun inlinedAnonymous(crossinline lambda: () -> Unit) = object {

      fun bar() = lambda()

   }

// 2
fun problem() = inlinedAnonymous {
   /* the lambda inlining causes the error */
}

fun valid(lambda: () -> Unit) = inlinedAnonymous(lambda)
```

A simple solution, if performances are not a critical issue, is to remove the inline modifier from the function.

## How it works

This sections is intended for curious people and aims at describing the most specific parts of how this project
works.

### Automatic internal symbols detection

By using the [kotlinx-metadata](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlinx-metadata) parser
library, it is possible to know at compile time which symbols are ``internal`` and therefore hide it.

### Java hiding

Like the Kotlin [@JvmSynthetic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-synthetic/), this
annotation induce the generation of the JVM ``ACC_SYNTHETIC``, hiding symbols from Java sources.

### Kotlin hiding

To effectively hide elements from Kotlin, the plugin generates on the marked elements the
[@Deprecated](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecated/) annotation. This annotation used with
the [DeprecationLevel.HIDDEN](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecation-level/-h-i-d-d-e-n.html)
level, makes the element invisible to Kotlin sources, but still visible to Java sources.

```kotlin
// Foo.kt
@HideFromKotlin("java only")
class Foo {
    // ...
}

// will be compiled to

// Foo.class
@HideFromKotlin
@Deprecated("java only", DeprecationLevel.HIDDEN)
class Foo {
    // ...
}
```

**NOTE:** The message of the `HideFromKotlin` annotation will be transferred to the `Deprecated` annotation.

Generating the `Deprecated` annotation or simply using it directly have slightly different outcomes. Indeed, the
`Deprecated` annotation (with `HIDDEN` level) acts as a flag for the Kotlin compiler. The latter will add the JVM
`ACC_SYNTHETIC` flag for the element in the produced classfile, making it also invisible for Java sources. The hack is
that the Kotlin compiler runs before calling the compiler plugin, so when it writes the classfile, the `Deprecated`
annotation is not present meaning that the `ACC_SYNTHETIC` flag is not set.

### Compilation order workaround

The main difficulty while developing Kotlin compiler plugins is that.

1. Metadata is parsed at the end of the file parsing, meaning that all "Kotlin informations" are known when the entire
   file has already been defined.
2. Symbol annotation processing is done after that the involved symbol has been declared, which is problem when it comes
   to change a symbol modifiers depending on its annotations.

To solve both of these problems, this project uses a singleton representing a context. Each writing instruction is
delayed by queueing the associated action (as a ``() -> Unit`` lambda). These actions can reference external values that
aren't already resolved until the right symbol is parsed, like demonstrated below (simplified example):

```kotlin
// will be filled by an other function
lateinit var isInternal: Boolean

fun functionDeclaration(signature: String, modifiers: Int, /* ... */) {

   Context.queue { // when this lambda will be executed, isInternal value will have a value
      val actualModifiers = if (isInternal) modifier or Modifiers.SYNTHETIC else actualModifiers
      writeFunctionDeclaration(signature, actualModifiers, /* ... */)
   }
}
```

Finally, when the last symbol of the file has been correctly queued, all lambdas are called in the right order,
to keep output classfile integrity.

## Future Plans

- Add support for generating annotations on all ``public`` (to be able to differentiate ``internal`` and ``public``)
  symbols of a project to simplify Kotlin project obfuscation with [ProGuard](https://www.guardsquare.com/proguard).
