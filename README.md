<div align="center">
<h1>Restrikt 2.0</h1>

<h4>Gradle Plugin</h4>
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.zwendo.restrikt2?color=%2366dcb8&logo=gradle)](https://plugins.gradle.org/plugin/com.zwendo.restrikt2)

<h4>Compiler Plugin</h4>
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt2-compiler-plugin)](https://search.maven.org/artifact/com.zwendo/restrikt2-compiler-plugin)

<h4>Annotations</h4>
[![Maven Central](https://img.shields.io/maven-central/v/com.zwendo/restrikt2-annotations)](https://search.maven.org/artifact/com.zwendo/restrikt2-annotations)

<h4>Others</h4>
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7f52ff.svg?logo=kotlin)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-8-%23ED8B00.svg?logo=openJdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)

<h3><i>A Kotlin/JVM compiler plugin to restrict symbols access, from external project sources. Compatible with
K2.</i></h3>
</div>

<br/>

### **Current features:**

- **Automatic way to hide symbols, with the automatic hiding of `internal` symbols.**
- **Manual way to hide symbols, by using annotations to hide symbols from either Kotlin or Java sources.**
- **Possibility to use the `package-private` visibility thanks to annotations.**
- **Generation of private constructors for top-level classes.**

## Summary

1. [Dependency](#dependency)
    1. [Gradle](#using-gradle)
    2. [Maven](#using-maven)
    3. [Kotlin Compiler](#using-kotlinc-in-the-command-line)
2. [Plugin Configuration](#plugin-configuration)
    1. [Available options](#available-options)
    2. [Gradle](#gradle)
    3. [Maven](#maven)
    4. [Kotlin Compiler](#kotlinc-in-the-command-line)
3. [Usage](#usage)
    1. [Internal symbols hiding](#internal-symbols-hiding)
    2. [Private constructors for Top-level classes](#private-constructors-for-top-level-classes)
    3. ['Hide' Annotations](#hide-annotations)
    4. [PackagePrivate annotation](#packageprivate-annotation)
    5. [Important notes](#important-notes)
4. [Known issues](#known-issues)
5. [How it works](#how-it-works)
6. [Future plans](#future-plans)
7. [Changelog](#changelog)

## Dependency

This compiler plugin offers some features working by detecting annotations on symbols. It allows you to define your own
annotations, but also provides some default annotations recognized by the plugin. You can add them to your dependencies
as regular dependencies.

> [!NOTE]
> See the [Plugin Configuration](#plugin-configuration) section for more information about the default annotations.

### Using Gradle

<details>
    <summary>Click to expand</summary>

Using Kotlin DSL:

```kotlin
plugins {
    id("com.zwendo.restrikt2") version "[latest-version]"
}
```

Using Groovy DSL:

```groovy
plugins {
    id 'com.zwendo.restrikt2' version '[latest-version]'
}
```

To add the default annotations to your project, you can add the following dependencies:

Using Kotlin DSL:

```kotlin
dependencies {
    implementation("com.zwendo:restrikt2-annotations:[latest-version]")
}
```

Using Groovy DSL:

```groovy
dependencies {
    implementation 'com.zwendo:restrikt2-annotations:[latest-version]'
}
```

</details>

### Using Maven

> [!WARNING]  
> Maven support does not work at the moment. However, to use the plugin maven if the support is added, it should be
> as follows:

<details>
    <summary>Click to expand</summary>

First of all, you need to add the compiler plugin to the kotlin's maven plugin dependencies:

```xml
<!-- ... -->
<plugins>
    <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
        <version>[kotlin-version]</version>
        <!-- rest of the plugin configuration... -->

        <dependencies>
            <dependency>
                <groupId>com.zwendo</groupId>
                <artifactId>restrikt2-compiler-plugin</artifactId>
                <version>[latest-version]</version>
            </dependency>
        </dependencies>
    </plugin>
    <!-- other plugins... -->
</plugins><!-- ... -->
```

The second step is to add the plugin id to the list of compiler plugins:

```xml
<!-- ... -->
<plugins>
    <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
        <version>[kotlin-version]</version>

        <configuration>
            <compilerPlugins>
                <plugin>com.zwendo.restrikt2</plugin>
            </compilerPlugins>
        </configuration>

        <!-- rest of the plugin configuration... -->
    </plugin>
    <!-- other plugins... -->
</plugins><!-- ... -->
```

Your `pom.xml` should look like this:

```xml

<project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>[kotlin-version]</version>
                <!-- ... -->

                <configuration>
                    <compilerPlugins>
                        <plugin>com.zwendo.restrikt2</plugin>
                    </compilerPlugins>
                </configuration>

                <dependencies>
                    <dependency>
                        <groupId>com.zwendo</groupId>
                        <artifactId>restrikt2-compiler-plugin</artifactId>
                        <version>[latest-version]</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

To add the default annotations to your project, you can add the following dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>com.zwendo</groupId>
        <artifactId>restrikt2-annotations</artifactId>
        <version>[latest-version]</version>
    </dependency>
</dependencies>
```
</details>

### Using Kotlinc in the command line

<details>
    <summary>Click to expand</summary>

To use the plugin with the Kotlin compiler, you simply need to add the plugin to the list of compiler plugins used in
your compilation process. It can be done by adding the following option to the `kotlinc` command:

```shell
-Xplugin=path/to/the/restrikt2-compiler-plugin.jar
```

Note that you might need to download the plugin jar. You can find it on
the [Maven Central Repository](https://search.maven.org/artifact/com.zwendo/restrikt2-compiler-plugin).

To the add default annotations to your project, you simply need to add the jars to the classpath of the compiler.

</details>

## Plugin Configuration

### Available options

> [!IMPORTANT]
> This section contains important information about the syntax to add annotations to the plugin configuration.

<details>
    <summary>Click to expand</summary>

Here are the currently supported default configuration options:

|              name              |             type              |           default            | allow multiple occurrences | description                                                                                       |
|:------------------------------:|:-----------------------------:|:----------------------------:|:--------------------------:|---------------------------------------------------------------------------------------------------|
|           `enabled`            |            boolean            |            `true`            |          `false`           | Whether the plugin is enabled.                                                                    |
|  `automatic-internal-hiding`   |            boolean            |            `true`            |          `false`           | Whether the internal symbols should be automatically hidden.                                      |
| `toplevel-private-constructor` |            boolean            |            `true`            |          `false`           | Whether to generate private constructor for top-level classes.                                    |
|    `annotation-processing`     |            boolean            |            `true`            |          `false`           | Whether the plugin annotations should be parsed to manually hide symbols.                         |
|  `hide-from-java-annotation`   | string [(1)](#options-note-1) | `none`[(2)](#options-note-2) |           `true`           | Adds an annotation to the annotations marking elements as hidden from Java.                       |
| `hide-from-kotlin-annotation`  | string [(1)](#options-note-1) | `none`[(2)](#options-note-2) |           `true`           | Adds an annotation to the annotations marking elements as hidden from Kotlin.                     |
|  `package-private-annotation`  | string [(1)](#options-note-1) | `none`[(2)](#options-note-2) |           `true`           | Adds an annotation to the annotations marking elements as package-private.                        |
|  `ignore-default-annotations`  |            boolean            |           `false`            |          `false`           | Whether to ignore default marking annotations when processing annotations. [(3)](#options-note-3) |

<a id="options-note-1"></a>
> [!IMPORTANT]  
> **Note 1:** The syntax to add annotation with any option accepting an annotation is its fully qualified name where all
> packages
> are separated by a slash (`/`) and all inner classes are separated by a dot (`.`).
>
> Here are few examples:
> -A `Foo` annotation declared in the `a.b.c` package could be added with `a/b/c/Foo`.
> -A `Bar` annotation declared in the `Base` class itself in the `bar` package could be added with `bar/Base.Bar`.

<a id="options-note-2"></a>
**Note 2:** `none` means that no annotation is added by default, except for the annotations controlled by the
`ignore-default-annotations` option.

<a id="options-note-3"></a>
**Note 2:** Default annotations are:

- `com/zwendo/restrikt2/annotation/HideFromKotlin`
- `com/zwendo/restrikt2/annotation/HideFromJava`
- `com/zwendo/restrikt2/annotation/PackagePrivate`

> [!IMPORTANT]
> Any custom annotation added to the plugin must have the `BINARY` or `RUNTIME` retention policy, for the plugin to be
> able to see it during the compilation process.

</details>

### Gradle

<details>
    <summary>Click to expand</summary>

> [!NOTE]  
> To follow the kotlin/groovy camelCase convention, options listed above will have their names in camelCase in the DSL.
>
> Moreover, the `hide-from-java-annotation`, `hide-from-kotlin-annotation` and `package-private-annotation` options will
> be renamed to `hideFromJavaAnnotations`, `hideFromKotlinAnnotations` and `packagePrivateAnnotations` respectively, and
> will be sets of strings instead of single strings.

You can configure the plugin using the configuration DSL.

```kotlin
restrikt2 {
    enabled = true
    hideFromJavaAnnotations = setOf("com/example/MyAnnotation", "com/example/MyOtherAnnotation")
    // ...
}
```

</details>

### Maven

<details>
    <summary>Click to expand</summary>

To configure the plugin with maven, you need to pass the configuration options as properties in the plugin
configuration.

Here is an example of options configuration:

```xml
<pluginOptions>
    <option>com.zwendo.restrikt2:enabled=true</option>
    <option>com.zwendo.restrikt2:annotation-processing=false</option>
</pluginOptions>
```

Using the example above and the configuration presented in the [Dependency](#dependency) section, your `pom.xml` should
look like this:

```xml
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>[kotlin-version]</version>

                <configuration>
                    <compilerPlugins>
                        <plugin>com.zwendo.restrikt2</plugin>
                    </compilerPlugins>

                    <pluginOptions>
                        <option>com.zwendo.restrikt2:enabled=true</option>
                        <option>com.zwendo.restrikt2:annotation-processing=false</option>
                    </pluginOptions>
                </configuration>

                <dependencies>
                    <dependency>
                        <groupId>com.zwendo</groupId>
                        <artifactId>restrikt2-compiler-plugin</artifactId>
                        <version>[latest-version]</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

</details>

### Kotlinc in the command line

<details>
    <summary>Click to expand</summary>


To configure the plugin with the Kotlin compiler, you need to pass the configuration options as arguments
`-P plugin:com.zwendo.restrikt2:[option-name]=[option-value]`.

Here is a concrete example:

```shell
-P plugin:com.zwendo.restrikt2:enabled=true
```

</details>

## Usage

### Internal symbols hiding

Restrikt plugin features automatic hiding from internal symbols in Kotlin sources. At compile time, all symbols with the
`internal` visibility automatically receives the JVM `ACC_SYNTHETIC` flag, making them invisible to Java sources.

Thus, the following code:

```kotlin
// Foo.kt
internal class Foo {
    fun bar() {
        // ...
    }
}

internal fun baz() {
    // ...
}
```

Will be compiled to:

```java
class Foo {
   // $FF: synthetic method
    public void bar() {
        
    }
}

class FooKt {
    // $FF: synthetic method
    public static void baz() {
        // ...
    }
}
```

### Private constructors for Top-level classes

Restrikt plugin also features the generation of private constructors for top-level classes. This is done to prevent
instantiation of top-level classes from Java sources.

It will compile the following code:

```kotlin
// Foo.kt
fun bar() {
    // ...
}
```

To:

```java
public final class Foo {
    private Foo() {
        throw new AssertionError();
    }

    public static void bar() {
        // ...
    }
}
```

### 'Hide' annotations

> [!NOTE]
> This section will only talk and use the default annotations in the examples, but everything shown here works the same
> way with custom annotations.

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

### PackagePrivate annotation

Because sometimes you just want to hide code to the outside of your package, and because Kotlin doesn't provide a
`package-private` visibility modifier, Restrikt plugin provides a `PackagePrivate` annotation to hide symbols from
outside their package.

### Important notes

- All elements hidden by a 'Hide' annotation will still be accessible at runtime, meaning that already compiled code
  will still be able to access it ;

## Known issues

<h4>Problems listed below are in the process of being resolved. If you encounter an issue that doesn't seem to be in
this list, feel free to open an issue for it.</h4>

<br/>

### Limitation on platform specific elements

Due to the way compiler plugins work (they are called before platform specific elements are generated), the plugin
cannot perform certain operations on platform specific elements. Here are the current known limitations:
- Apply visibility restrictions on value class members ;
- Generate private constructor to MultiFileClass facades.

## How it works

This section is intended for curious people and aims at describing the most specific parts of how this project
works.

### Java hiding

Like the Kotlin [@JvmSynthetic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-synthetic/), marked
elements will have an `ACC_SYNTHETIC` accessor added to them, hiding class members from Java sources. As for classes,
because the `ACC_SYNTHETIC` doesn't work on them, the flag is applied to all the class members instead.

### Kotlin hiding

To effectively hide elements from Kotlin, the plugin generates on the marked elements the
[@Deprecated](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecated/) annotation. This annotation used with
the [DeprecationLevel.HIDDEN](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecation-level/-h-i-d-d-e-n.html)
level, makes the element invisible to Kotlin sources, but still visible to Java sources.

```kotlin
// Foo.kt
@HideFromKotlin
class Foo {
    // ...
}

// will be compiled to ...

// Foo.class
@HideFromKotlin
@Deprecated(message = "", level = DeprecationLevel.HIDDEN)
class Foo {
    // ...
}
```

Generating the `Deprecated` annotation or simply using it directly have slightly different outcomes. Indeed, the
`Deprecated` annotation (with `HIDDEN` level) acts as a flag for the Kotlin compiler. The latter will add the JVM
`ACC_SYNTHETIC` flag for the element in the produced classfile, making it also invisible for Java sources. The hack is
that the Kotlin compiler processing of `Deprecated` annotations runs **before** the compiler plugin runs, so the
`Deprecated` annotations will not be present at the time the compiler processes the symbols.

## Future Plans

- Add support for generating annotations on all `public` (to be able to differentiate `internal` and `public`)
  symbols of a project to simplify Kotlin project obfuscation with [ProGuard](https://www.guardsquare.com/proguard).

## Changelog

### 0.1.1 - 2024-10-11

**Bugfixes** :

- Fixed parent state transitivity for fields and property methods ;
- Disabled plugin on Value classes as it did not work properly.

### 0.1.0 - 2024-10-11

- Initial release of the plugin.
