package com.zwendo.restrikt.maven

class AnnotationConfiguration internal constructor() {

    var enabled: Boolean? = null

    var keepAnnotation: Boolean? = null

    var defaultReason: String? = null
    override fun toString(): String {
        return "AnnotationConfiguration(enabled=$enabled, keepAnnotation=$keepAnnotation, defaultReason=$defaultReason)"
    }

}
