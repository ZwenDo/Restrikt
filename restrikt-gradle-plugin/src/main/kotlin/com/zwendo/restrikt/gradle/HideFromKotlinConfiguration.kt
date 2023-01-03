package com.zwendo.restrikt.gradle

import com.zwendo.restrikt_gradle_plugin.BuildConfig

class HideFromKotlinConfiguration : AbstractAnnotationConfiguration() {

    internal var deprecatedMessageField: String? = null

    /**
     * The deprecation message used when [Deprecated] annotations are generated.
     */
    var deprecatedMessage: String
        get() = deprecatedMessageField ?: BuildConfig.DEPRECATED_DEFAULT_REASON
        set(value) {
            deprecatedMessageField = value
        }

}
