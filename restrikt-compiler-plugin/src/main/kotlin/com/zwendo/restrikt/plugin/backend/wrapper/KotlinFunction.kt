package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction

internal class KotlinFunction(private val signature: String) {

    private var isInternal: Boolean = false

    fun isSynthetic(originClass: KotlinClass): Boolean =
        (PluginConfiguration.automaticInternalHiding && isInternal) || originClass.isForceSynthetic(signature)


    fun setData(inner: KmFunction) {
        isInternal = Flag.IS_INTERNAL(inner.flags)
    }

    fun setData(inner: KmConstructor) {
        isInternal = Flag.IS_INTERNAL(inner.flags)
    }

    fun setInternal() {
        isInternal = true
    }

}
