package com.zwendo.restrikt.plugin.frontend

internal object PluginConfiguration {

    var enabled = true

    var toplevelPrivateConstructor = true

    var automaticInternalHiding = true

    var annotationProcessing = true

    var hideFromJava = AnnotationConfiguration()

    var hideFromKotlin = HideFromKotlinConfiguration()

    var packagePrivate = AnnotationConfiguration()

    open class AnnotationConfiguration {

        var enabled = true
            get() = annotationProcessing && field

        var retention = Retention.BINARY

        var defaultReason: String? = null

        enum class Retention {
            SOURCE,
            BINARY,
            RUNTIME,
            ;

            val isRuntime: Boolean
                get() = this == RUNTIME

            val writeToClassFile: Boolean
                get() = this == RUNTIME || this == BINARY

        }

    }

    class HideFromKotlinConfiguration : AnnotationConfiguration() {

        private var deprecatedReasonField: String? = null

        var deprecatedReason: String
            get() = deprecatedReasonField ?: HIDE_FROM_KOTLIN_DEPRECATED_DEFAULT_REASON
            set(value) {
                deprecatedReasonField = value
            }

    }

    private const val HIDE_FROM_KOTLIN_DEPRECATED_DEFAULT_REASON: String = "This element is hidden from Kotlin."

}
