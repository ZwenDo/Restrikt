package com.zwendo.restrikt.gradle

/**
 * Configuration options for the Restrikt plugin.
 */
open class RestriktConfiguration {

    /**
     * Whether the compiler plugin should run.
     */
    var enabled = true

    /**
     * Whether plugin annotations should be kept or removed in the generated code.
     */
    var keepAnnotations = true
}
