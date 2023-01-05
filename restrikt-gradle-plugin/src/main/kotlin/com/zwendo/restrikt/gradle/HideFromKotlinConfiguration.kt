package com.zwendo.restrikt.gradle

class HideFromKotlinConfiguration : AbstractAnnotationConfiguration() {

    /**
     * The deprecation message used when [Deprecated] annotations are generated.
     */
    var deprecatedMessage: String? = null

}
