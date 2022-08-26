package com.zwendo.restrikt.gradle

class AnnotationConfiguration internal constructor() {

    /**
     * Whether the compiler plugin should run.
     */
    var enabled: Boolean? = null

    /**
     * Whether plugin annotations should be kept or removed in the generated code.
     */
    var keepAnnotation: Boolean? = null

    /**
     * The default reason that will be used when no reason is provided.
     */
    var defaultReason: String? = null

}
