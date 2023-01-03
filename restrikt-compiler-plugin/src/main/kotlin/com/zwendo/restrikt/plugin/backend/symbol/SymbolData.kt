package com.zwendo.restrikt.plugin.backend.symbol

internal sealed interface SymbolData<E> {

    fun setPackagePrivate()

    fun hideFromJava()

    fun hideFromKotlin()

    fun queueAction(action: E.() -> Unit)

    val canQueue: Boolean

}
