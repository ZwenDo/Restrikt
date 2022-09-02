package com.zwendo.restrikt.plugin.backend

import org.jetbrains.org.objectweb.asm.Opcodes


internal const val ASM_VERSION = Opcodes.ASM9

private const val PACKAGE_PRIVATE_MASK =
    0.inv() xor Opcodes.ACC_PUBLIC xor Opcodes.ACC_PRIVATE xor Opcodes.ACC_PROTECTED

internal fun Int.setPackagePrivate() = this and PACKAGE_PRIVATE_MASK
