package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.KmProperty
import org.jetbrains.org.objectweb.asm.Opcodes

internal class KotlinProperty(private val name: String) {

    private var isInternal = false

    private var isForceSynthetic = false

    private var isPackagePrivate = false

    private val isSynthetic
        get() = (PluginConfiguration.automaticInternalHiding && isInternal) || isForceSynthetic

    fun setData(property: KmProperty) {
        isInternal = Flag.IS_INTERNAL(property.flags)
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

        return actualAccess
    }

    fun applyModifiersToFunction(function: KotlinFunction, annotationFunction: KotlinFunction?) {
        if (isInternal || annotationFunction?.isInternal == true) {
            function.setInternal()
        }

        if (isForceSynthetic || annotationFunction?.isForceSynthetic == true) {
            function.forceSynthetic()
        }

        if (isPackagePrivate || annotationFunction?.isPackagePrivate == true) {
            function.setPackagePrivate()
        }
    }

}
