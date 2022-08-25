package com.zwendo.restrikt.plugin.backend.wrapper

import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction

internal interface KotlinFunction : KotlinSymbol {

    companion object {

        fun new(inner: KmFunction): KotlinFunction = Impl { Flag.IS_INTERNAL(inner.flags) }

        fun new(inner: KmConstructor): KotlinFunction = Impl { Flag.IS_INTERNAL(inner.flags) }

    }


    private class Impl(internalStateProvider: () -> Boolean) : KotlinFunction {

        private var forceSynthetic = false

        override val isInternal: Boolean = internalStateProvider()

        override val isForceSynthetic: Boolean = false

        override fun forceSynthetic() {
            forceSynthetic = true
        }

    }

}
