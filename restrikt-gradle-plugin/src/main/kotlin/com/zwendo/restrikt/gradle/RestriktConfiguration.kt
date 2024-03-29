package com.zwendo.restrikt.gradle

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
     * The default retention policy for the annotations.
     *
     * **Default:** `BINARY`
     */
    var defaultRetentionPolicy: RestriktAnnotationRetention? = null

    /**
     * Configure the HideFromJava annotation.
     */
    fun hideFromJava(block: AnnotationConfiguration.() -> Unit) = hideFromJava.block()

    /**
     * Configure the HideFromKotlin annotation.
     */
    fun hideFromKotlin(block: HideFromKotlinConfiguration.() -> Unit) = hideFromKotlin.block()

    /**
     * Configure the PackagePrivate annotation.
     */
    fun packagePrivate(block: AnnotationConfiguration.() -> Unit) = packagePrivate.block()

    internal val hideFromJava = AnnotationConfiguration()

    internal val hideFromKotlin = HideFromKotlinConfiguration()

    internal val packagePrivate = AnnotationConfiguration()

}
