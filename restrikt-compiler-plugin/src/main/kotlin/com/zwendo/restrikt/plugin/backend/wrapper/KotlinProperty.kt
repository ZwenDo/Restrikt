package com.zwendo.restrikt.plugin.backend.wrapper

import kotlinx.metadata.Flag
import kotlinx.metadata.KmProperty

internal class KotlinProperty(inner: KmProperty) : KotlinSymbol {

    private var forceSynthetic = false

    override val isInternal: Boolean = Flag.Common.IS_INTERNAL(inner.flags)

    override val isForceSynthetic: Boolean = forceSynthetic

    override fun forceSynthetic() {
        forceSynthetic = true
    }

}
