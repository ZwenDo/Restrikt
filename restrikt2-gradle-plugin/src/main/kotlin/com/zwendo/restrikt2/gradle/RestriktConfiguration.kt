package com.zwendo.restrikt2.gradle

/**
 * Configuration options for the Restrikt plugin.
 */
open class RestriktConfiguration {

    /**
     * Whether to enable the Restrikt plugin.
     *
     * **Default:** `true`
     */
    var enabled: Boolean? = null

    /**
     * Whether to generate private constructors toplevel classes.
     *
     * **Default:** `true`
     */
    var toplevelPrivateConstructor: Boolean? = null

    /**
     * Whether internal symbols should be automatically hidden to java.
     *
     * **Default:** `true`
     */
    var automaticInternalHiding: Boolean? = null

    /**
     * Whether the annotation processing should be enabled.
     *
     * **Default:** `true`
     */
    var annotationProcessing: Boolean? = null

    /**
     * Whether to ignore default annotations.
     *
     * **Default:** `false`
     *
     * Default annotations are:
     * - `com/zwendo/restrikt/annotation/HideFromJava` for hiding symbols from Java
     * - `com/zwendo/restrikt/annotation/HideFromKotlin` for hiding symbols from Kotlin
     * - `com/zwendo/restrikt/annotation/PackagePrivate` for marking symbols as package-private
     */
    var ignoreDefaultAnnotations: Boolean? = null

    /**
     * Annotations marking symbols that should be hidden from Java.
     *
     * **Default:** `none` (see [ignoreDefaultAnnotations])
     *
     * Annotations can be added using the fully qualified class name where the package name is separated by a `/` and
     * inner classes are separated by a `.`.
     *
     * Here is an example:
     *
     * ```kt
     * package foo.bar
     *
     * annotation class MyAnnotation
     *
     * object Foo {
     *     annotation class InnerAnnotation
     * }
     * ```
     *
     * The annotations declared above can be added to the configuration as follows:
     * - `foo/bar/MyAnnotation` for `MyAnnotation`
     * - `foo/bar/Foo.InnerAnnotation` for `Foo.InnerAnnotation`
     */
    var hideFromJavaAnnotations: Set<String> = emptySet()

    /**
     * Annotations marking symbols that should be hidden from Kotlin.
     *
     * **Default:** `none` (see [ignoreDefaultAnnotations])
     *
     * Annotations can be added using the fully qualified class name where the package name is separated by a `/` and
     * inner classes are separated by a `.`.
     *
     * Here is an example:
     *
     * ```kt
     * package foo.bar
     *
     * annotation class MyAnnotation
     *
     * object Foo {
     *     annotation class InnerAnnotation
     * }
     * ```
     *
     * The annotations declared above can be added to the configuration as follows:
     * - `foo/bar/MyAnnotation` for `MyAnnotation`
     * - `foo/bar/Foo.InnerAnnotation` for `Foo.InnerAnnotation`
     */
    var hideFromKotlinAnnotations: Set<String> = emptySet()

    /**
     * Annotations marking symbols that should be package-private.
     *
     * **Default:** `none` (see [ignoreDefaultAnnotations])
     *
     * Annotations can be added using the fully qualified class name where the package name is separated by a `/` and
     * inner classes are separated by a `.`.
     *
     * Here is an example:
     *
     * ```kt
     * package foo.bar
     *
     * annotation class MyAnnotation
     *
     * object Foo {
     *     annotation class InnerAnnotation
     * }
     * ```
     *
     * The annotations declared above can be added to the configuration as follows:
     * - `foo/bar/MyAnnotation` for `MyAnnotation`
     * - `foo/bar/Foo.InnerAnnotation` for `Foo.InnerAnnotation`
     */
    var packagePrivateAnnotations: Set<String> = emptySet()

}
