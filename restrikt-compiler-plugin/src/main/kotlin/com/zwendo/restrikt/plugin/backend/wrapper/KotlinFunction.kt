package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.signature

internal interface KotlinFunction {

    fun isSynthetic(originClass: KotlinClass): Boolean

    companion object {

        fun new(inner: KmFunction): KotlinFunction = Impl(
            "${inner.signature?.asString()}",
            Flag.IS_INTERNAL(inner.flags)
        )

        fun new(inner: KmConstructor): KotlinFunction = Impl(
            "<init>${inner.signature?.asString()}",
            Flag.IS_INTERNAL(inner.flags)
        )

        fun new(name: String): KotlinFunction = Impl(name, false)

    }

    private class Impl(
        private val innerName: String,
        private val isInternal: Boolean,
    ) : KotlinFunction {

        override fun isSynthetic(originClass: KotlinClass): Boolean =
            (PluginConfiguration.automaticInternalHiding && isInternal) || originClass.isForceSynthetic(innerName)

    }

}
