package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.backend.PACKAGE_PRIVATE_MASK
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmProperty
import org.jetbrains.org.objectweb.asm.Opcodes

internal class KotlinProperty : KotlinSymbol {

    private var isInternal = false

    private var isForceSynthetic = false

    private var isPackagePrivate = false

    private val isSynthetic
        get() = (PluginConfiguration.automaticInternalHiding && isInternal) || isForceSynthetic

    fun setData(property: KmProperty) {
        isInternal = Flag.IS_INTERNAL(property.flags)
    }

    override fun forceSynthetic() {
        isForceSynthetic = true
    }

    override fun setPackagePrivate() {
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

    fun applyModifiersToFunction(function: KotlinFunction, annotationFunction: KotlinFunction?) {
        if (isInternal || annotationFunction?.isInternal == true) {
            function.setInternal()
        }

        if (annotationFunction?.isForceSynthetic == true) {
            function.forceSynthetic()
        }

        if (annotationFunction?.isPackagePrivate == true) {
            function.setPackagePrivate()
        }
    }

}
