package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmProperty

internal class KotlinProperty(private val inner: KmProperty) {

    val isInternal: Boolean = Flag.Common.IS_INTERNAL(inner.flags)

    fun isSynthetic(originClass: KotlinClass): Boolean = (PluginConfiguration.automaticInternalHiding && isInternal)
                || originClass.isForceSynthetic(inner.name)

}
