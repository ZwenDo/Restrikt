package com.zwendo.restrikt.gradle

sealed class AbstractAnnotationConfiguration {

    /**
     * Whether the compiler plugin should run.
     *
     * **Default:** `true`
     */
    var enabled: Boolean? = null

    /**
     * The retention policy of the annotation.
     *
     * **Default:** `BINARY`
     */
    var retention: RestriktAnnotationRetention? = null

    /**
     * The default reason that will be used when no reason is provided.
     *
     * **Default:** none
     */
    var defaultReason: String? = null

}
