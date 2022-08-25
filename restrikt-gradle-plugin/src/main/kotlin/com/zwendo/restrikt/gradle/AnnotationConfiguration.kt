package com.zwendo.restrikt.gradle

import com.zwendo.restrikt_gradle_plugin.BuildConfig

class AnnotationConfiguration internal constructor(lang: String) {

    /**
     * Whether the compiler plugin should run.
     */
    var enabled: Boolean = true

    /**
     * Whether plugin annotations should be kept or removed in the generated code.
     */
    var keepAnnotation: Boolean = true

    /**
     * The default reason that will be used when no reason is provided.
     */
    var defaultReason: String = BuildConfig.DEFAULT_REASON.replace("{lang}", lang)

}
