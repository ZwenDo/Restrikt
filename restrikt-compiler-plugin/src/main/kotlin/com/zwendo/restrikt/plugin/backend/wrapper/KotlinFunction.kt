package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.backend.PACKAGE_PRIVATE_MASK
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import org.jetbrains.org.objectweb.asm.Opcodes

internal class KotlinFunction(private val signature: String) {

    var isInternal = false
        private set

    var isPackagePrivate = false
        private set

    var isForceSynthetic = false

    private val isSynthetic
        get() = (PluginConfiguration.automaticInternalHiding && isInternal) || isForceSynthetic

    fun setData(inner: KmFunction) {
        isInternal = Flag.IS_INTERNAL(inner.flags)
    }

    fun setData(inner: KmConstructor) {
        isInternal = Flag.IS_INTERNAL(inner.flags)
    }

    fun setInternal() {
        isInternal = true
    }

    fun forceSynthetic() {
        isForceSynthetic = true
    }

    fun setPackagePrivate() {
        isPackagePrivate = true
    }

    fun computeModifiers(access: Int): Int {
        var actualAccess: Int = access

        if (isSynthetic) {
            actualAccess = actualAccess or Opcodes.ACC_SYNTHETIC
        }

        if (isPackagePrivate) {
            actualAccess = actualAccess and PACKAGE_PRIVATE_MASK
        }

        return actualAccess
    }

    fun removeAll() {
        isInternal = false
        isPackagePrivate = false
    }

}
