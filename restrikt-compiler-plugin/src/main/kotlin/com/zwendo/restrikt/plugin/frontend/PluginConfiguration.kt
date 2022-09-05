package com.zwendo.restrikt.plugin.frontend

internal object PluginConfiguration {

    var toplevelPrivateConstructor = true

    var automaticInternalHiding = true

    var annotationProcessing = true

    var hideFromJava = AnnotationConfiguration()

    var hideFromKotlin = AnnotationConfiguration()

    var packagePrivate = AnnotationConfiguration()

    class AnnotationConfiguration {

        var enabled = true
            get() = annotationProcessing && field

        var keepAnnotation = true

        var defaultReason: String? = null

    }

}
