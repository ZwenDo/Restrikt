package com.zwendo.restrikt.plugin.backend.symbol

internal sealed interface WritableSymbolData<E> : SymbolData<E> {

    fun writeToClassFile(forceWriting: Boolean)

    val isWritten: Boolean

}
