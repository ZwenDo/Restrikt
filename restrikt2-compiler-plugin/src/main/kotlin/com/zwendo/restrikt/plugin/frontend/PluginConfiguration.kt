package com.zwendo.restrikt.plugin.frontend

import org.jetbrains.kotlin.it.unimi.dsi.fastutil.Hash
import org.jetbrains.kotlin.name.ClassId

/**
 * Global configuration for the plugin. This object stores the configuration of the plugin. It should only be mutated
 * during the command line processing.
 */
internal object PluginConfiguration {

    /**
     * Whether the plugin is enabled or not.
     */
    var enabled: Boolean = true

    /**
     * Whether to generate private constructors for top-level classes.
     */
    var toplevelPrivateConstructor: Boolean = true

    /**
     * Whether to automatically hide internal symbols.
     */
    var automaticInternalHiding: Boolean = true

    /**
     * Whether to process annotations.
     */
    var annotationProcessing: Boolean = true

    /**
     * Set of annotations that mark elements that should be hidden from Java.
     */
    val hideFromJavaAnnotations: HashSet<ClassId> = HashSet()

    /**
     * Set of annotations that mark elements that should be hidden from Kotlin.
     */
    val hideFromKotlinAnnotations: HashSet<ClassId> = HashSet()

    /**
     * Set of annotations that mark elements that should be package private.
     */
    val packagePrivateAnnotations: HashSet<ClassId> = HashSet()

    private val defaults: HashSet<Pair<ClassId, HashSet<ClassId>>> = HashSet()

    init {
        val defaultAnnotationsPackage = "com/zwendo/restrikt2/annotation/"
        val legacyDefaultAnnotationsPackage = "com/zwendo/restrikt/annotation/"
        arrayOf(
            "HideFromJava" to hideFromJavaAnnotations,
            "HideFromKotlin" to hideFromKotlinAnnotations,
            "PackagePrivate" to packagePrivateAnnotations
        ).forEach {
            val (annotation, set) = it

            val default = ClassId.fromString(defaultAnnotationsPackage + annotation)
            set.add(default)
            defaults.add(default to set)

            val legacyDefault = ClassId.fromString(legacyDefaultAnnotationsPackage + annotation)
            set.add(legacyDefault)
            defaults.add(legacyDefault to set)
        }
    }

    fun removeDefaults() {
        defaults.forEach { (default, set) ->
            set.remove(default)
        }
    }

}
