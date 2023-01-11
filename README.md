# Restrikt

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.zwendo.restrikt?color=%2366dcb8&logo=gradle)](https://plugins.gradle.org/plugin/com.zwendo.restrikt)
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt-annotation)](https://search.maven.org/artifact/com.zwendo/restrikt-annotation)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.21-7f52ff.svg?logo=kotlin)](https://kotlinlang.org)

<div align="center">
<h3><i>A Kotlin/JVM compiler plugin to restrict symbols access, from external project sources.</i></h3>
</div>

<br/>


**Current features:**

- **Automatic way to hide symbols, with the automatic hiding of `internal` symbols ;**
- **Manual way to hide symbols, by using two different annotations to hide symbols from either Kotlin or Java sources
  ;**
- **Possibility to use the `package-private` visibility thanks to ann annotation ;**
- **Generation of private constructors for top-level classes .**

## Summary

1. [Dependency](#dependency)
   1. [Gradle plugins DSL](#using-the-gradle-plugin-dsl--gradle-21-)
   2. [Gradle apply method](#using-apply-method--gradle-prior-to-21-)
   3. [Maven](#using-maven)
2. [Plugin Configuration](#plugin-configuration)
   1. [Available options](#available-options)
   2. [Gradle](#gradle)
   3. [Maven](#maven)
3. [Usage](#usage)
   1. [Internal symbols hiding](#internal-symbols-hiding)
   2. [Private constructors for Top-level classes](#private-constructors-for-top-level-classes)
   3. ['Hide' Annotations](#-hide-annotations)
   4. [PackagePrivate annotation](#packageprivate-annotation)
   5. [Important notes](#important-notes)
4. [Known issues](#known-issues)
5. [How it works](#how-it-works)
6. [Future plans](#future-plans)
7. [Changelog](#changelog)

## Dependency

Both **compiler plugin** and **annotations** are added to your project's dependencies in the same unique way, as shown
below :

### Using the Gradle plugin DSL (Gradle 2.1+)

<details>
    <summary>Click to expand</summary>

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

</details>

### Using `apply` method (Gradle prior to 2.1)

<details>
    <summary>Click to expand</summary>

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

</details>

### Using Maven

<details>
    <summary>Click to expand</summary>

First of all, you need to add the compiler plugin to the kotlin's maven plugin dependencies:
```xml
<dependency>
    <groupId>com.zwendo</groupId>
    <artifactId>restrikt-compiler-plugin</artifactId>
    <version>[latest-version]</version>
</dependency>
```

Then, you need to add the annotations to your project's dependencies:
```xml
<dependency>
    <groupId>com.zwendo</groupId>
    <artifactId>restrikt-annotation</artifactId>
    <version>[latest-version]</version>
</dependency>
```

Your `pom.xml` should look like this:
```xml
<project>
    <!-- ... -->
    <build>
        <!-- ... -->
        <plugins>
            <!-- ... -->
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>[kotlin-version]</version>
                <!-- ... -->
                <dependencies>
                    <dependency>
                        <groupId>com.zwendo</groupId>
                        <artifactId>restrikt-compiler-plugin</artifactId>
                        <version>[latest-version]</version>
                    </dependency>
                    <!-- ... -->
                </dependencies>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>com.zwendo</groupId>
            <artifactId>restrikt-annotation</artifactId>
            <version>[latest-version]</version>
        </dependency>
    </dependencies>
</project>
```
</details>

## Plugin Configuration

### Available options

<details>
    <summary>Click to expand</summary>

Here are the currently supported default configuration options:

|             name             |  type   | default | description                                                               |
|:----------------------------:|:-------:|:-------:|---------------------------------------------------------------------------|
|          `enabled`           | boolean | `true`  | Whether the plugin is enabled                                             |
|  `automaticInternalHiding`   | boolean | `true`  | Whether the internal symbols should be automatically hidden.              |
|    `annotationProcessing`    | boolean | `true`  | Whether the plugin annotations should be parsed to manually hide symbols. |
| `toplevelPrivateConstructor` | boolean | `true`  | Whether to generate private constructor for top-level classes.            |

Moreover, all annotations of the plugin can be individually configured using their own DSL (`hideFromKotlin`,
`hideFromJava` or `packagePrivate`), with the following configuration options:

|      name       |  type   | default  | description                                                                                                   |
|:---------------:|:-------:|:--------:|---------------------------------------------------------------------------------------------------------------|
|    `enabled`    | boolean |  `true`  | Whether the annotation should be processed to hide symbols. (works only if `annotationProcessing` is `true`). |
|   `retention`   | boolean | `binary` | The retention policy of the annotation.                                                                       |
| `defaultReason` | string  |  `none`  | The default reason written on the annotation if no specific reason is provided.                               |

In addition to the options above, `HideFromKotlin` annotation can be configured using the following options:

|        name         |  type  | default | description                                        |
|:-------------------:|:------:|:-------:|----------------------------------------------------|
| `deprecatedMessage` | string | `none`  | The message written on the `Deprecated`annotation. |
</details>

### Gradle

<details>
    <summary>Click to expand</summary>

You can configure the plugin using the configuration DSL.

```kotlin
restrikt {
   option = value
   // ...
}
```

To configure the annotations, a DSL function is available for each annotation:

```kotlin
restrikt {
    hideFromKotlin {
        option = value
        // ...
    }
}
```
</details>

### Maven

Currently, the plugin is not configurable using Maven. The [default](#available-options) configuration
will be used.

## Usage

### Internal symbols hiding

Restrikt plugin features automatic hiding from internal symbols in Kotlin sources. At compile time, all symbols with the
`internal` visibility automatically receives the JVM `ACC_SYNTHETIC` flag, making them invisible to Java sources.

### Private constructors for Top-level classes

Restrikt plugin also features the generation of private constructors for top-level classes (classic top-level classes as
well as facade classes generated by
[@JvmMultifileClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-multifile-class/) annotation). This is
done to prevent instantiation of top-level classes from Java sources.

### 'Hide' annotations

This plugin provides two annotations intended for symbol access restrictions. These two annotations, namely
`HideFromJava` and `HideFromKotlin`, are used to hide symbols from Java and Kotlin sources respectively. They can be
used on several targets such as classes, functions, properties, getters, setters, field etc. They are designed to be
used in the same way, just by placing the right annotation on the symbol to hide as follows:

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

### PackagePrivate annotation

Because sometimes you just want to hide code to the outside of your package, and because Kotlin doesn't provide a
`package-private` visibility modifier, Restrikt plugin provides a `PackagePrivate` annotation to hide symbols from
outside their package.

### Important notes

- All elements hidden by a 'Hide' annotation will still be accessible at runtime, meaning that already compiled code
  will still be able to access it ;
- Symbols hidden from Kotlin will still be accessible at compile-time from Kotlin sources in the same class (there is
  some very specific exceptions but the only access that always work is from the same class).
- Most IDEs won't warn you on the usage of a symbol made package-private by the `@PackagePrivate` annotation. However,
  at runtime, you will get an `IllegalAccessError` if you try to access it from outside its package.

## Known issues

<h4>Problems listed below are in the process of being resolved. If you encounter an issue that doesn't seem to be in
this list, feel free to open an issue for it.</h4>

<br/>

*All known issues have been resolved.*

## How it works

This section is intended for curious people and aims at describing the most specific parts of how this project
works.

### Java hiding

Like the Kotlin [@JvmSynthetic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-synthetic/), this
annotation induce the generation of the JVM `ACC_SYNTHETIC`, hiding class members from Java sources. As for classes,
because the `ACC_SYNTHETIC` doesn't work on them, the flag is applied to all the class members instead.

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

// will be compiled to ...

// Foo.class
@HideFromKotlin
@Deprecated("java only", DeprecationLevel.HIDDEN)
class Foo {
    // ...
}
```

Generating the `Deprecated` annotation or simply using it directly have slightly different outcomes. Indeed, the
`Deprecated` annotation (with `HIDDEN` level) acts as a flag for the Kotlin compiler. The latter will add the JVM
`ACC_SYNTHETIC` flag for the element in the produced classfile, making it also invisible for Java sources. The hack is
that the Kotlin compiler runs before calling the compiler plugin, so when it writes the classfile, the `Deprecated`
annotation is not present meaning that the `ACC_SYNTHETIC` flag is not set.


## Future Plans

- Add plugin support for maven projects ;
- Create a Restrikt IntelliJ plugin to prevent restricted symbols misuse ;
- Add support for generating annotations on all `public` (to be able to differentiate `internal` and `public`)
  symbols of a project to simplify Kotlin project obfuscation with [ProGuard](https://www.guardsquare.com/proguard).

## Changelog

### 3.0.1 - 2023-01-11

**Bugfixes** : 

- Fixed bug were Restrikt caused errors when compiling tests by disabling the plugin for test sources.

### 3.0.0 - 2023-01-09

**Breaking Changes** :

- Option `keepAnnotation` has been replaced by the new `retention` option.

**Features** :

- Annotation retentions are now individually configurable
- Slightly improved performance by using a more efficient way to retrieve information from symbols.
- Plugin now works with maven projects.

**Bugfix** :

- Plugin now works with inline functions
- Plugin now correctly hides multi file classes functions

### 2.1.0 - 2022.09.05

**Features** :

- Added the `PackagePrivate` annotation to force compiler to use the `package-private` visibility ;
- Plugin can now generate private constructors for top-level classes ;
- `HideFromJava` annotation now supports the File and Property targets ;
- `HideFromKotlin` annotation now supports the Property target.

**Bugfixes** :

- 'Hide' annotations now works correctly on annotation classes declarations ;
- Automatic internal hiding now works properly on constructors.

### 2.0.0 - 2022.08.27

**Features** :

- Automatic detection and hiding from internal symbols ;
- Added the `HideFromJava` annotation to hide symbols from Java sources ;
- New gradle plugin configuration options for each annotation and internal hiding.
