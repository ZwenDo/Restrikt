package com.zwendo.restrikt.gradle

/**
 * Configuration options for the Restrikt plugin.
 */
open class RestriktConfiguration {

    /**
     * Whether internal symbols should be automatically hidden to java.
     */
    var automaticInternalHiding: Boolean = true

    /**
     * Whether the annotation processing should be enabled.
     */
    var annotationProcessing: Boolean = true

    /**
     * Configure the HideFromJava annotation.
     */
    fun hideFromJava(block: AnnotationConfiguration.() -> Unit) = hideFromJava.block()

    /**
     * Configure the HideFromKotlin annotation.
     */
    fun hideFromKotlin(block: AnnotationConfiguration.() -> Unit) = hideFromKotlin.block()

    internal val hideFromJava = AnnotationConfiguration("Java")

    internal val hideFromKotlin = AnnotationConfiguration("Kotlin")

}
