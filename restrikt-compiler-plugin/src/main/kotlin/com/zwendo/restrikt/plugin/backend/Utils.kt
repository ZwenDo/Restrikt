package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.HideFromKotlin
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.Opcodes


internal const val ASM_VERSION = Opcodes.ASM9

internal val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc
