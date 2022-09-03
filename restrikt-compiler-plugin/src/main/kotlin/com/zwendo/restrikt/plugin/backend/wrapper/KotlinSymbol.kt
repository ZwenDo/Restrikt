package com.zwendo.restrikt.plugin.backend.wrapper

internal sealed interface KotlinSymbol {

    fun forceSynthetic()

    fun setPackagePrivate()

}
