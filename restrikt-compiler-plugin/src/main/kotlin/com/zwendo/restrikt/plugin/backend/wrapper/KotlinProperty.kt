package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmProperty

internal class KotlinProperty(private val name: String) {

    /**
     * Used by [KotlinClass] to determine if property functions must be hidden as well.
     */
    var isInternal: Boolean = false
        private set

    fun isSynthetic(originClass: KotlinClass): Boolean = (PluginConfiguration.automaticInternalHiding && isInternal)
                || originClass.isForceSynthetic(name)

    fun setData(property: KmProperty) {
        isInternal = Flag.IS_INTERNAL(property.flags)
    }

    var isPackagePrivate = false
        private set

    fun setPackagePrivate() {
        isPackagePrivate = true
    }

}
