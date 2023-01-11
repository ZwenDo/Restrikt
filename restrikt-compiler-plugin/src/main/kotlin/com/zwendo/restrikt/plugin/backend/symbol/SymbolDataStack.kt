package com.zwendo.restrikt.plugin.backend.symbol

internal class SymbolDataStack {

    private val stack = mutableListOf<WritableSymbolData<*>>()

    fun push(data: WritableSymbolData<*>) {
        stack += data
    }

    fun writeTop(forceWriting: Boolean) {
        if (stack.isEmpty()) return
        stack.last().writeToClassFile(forceWriting)
        stack.removeAll { it.isWritten }
    }

}
