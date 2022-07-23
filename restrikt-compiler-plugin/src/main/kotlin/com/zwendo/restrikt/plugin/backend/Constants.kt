package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.HideFromKotlin
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.Opcodes

internal object Constants {

    const val ASM_VERSION = Opcodes.ASM9

    @JvmField
    val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc

    const val REASON_PARAMETER_NAME = "reason"

    const val ALTERNATIVE_PARAMETER_NAME = "alternative"

}
