package com.zwendo.restrikt.plugin.backend.wrapper

/**
 * Interface to wrap kotlin symbols and facilitate the access to their visibility and modifiers for hiding purposes.
 */
internal sealed interface KotlinSymbol {

    val isInternal: Boolean

    val isForceSynthetic: Boolean

    fun forceSynthetic()

}