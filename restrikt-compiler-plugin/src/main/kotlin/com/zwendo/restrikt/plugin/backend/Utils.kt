package com.zwendo.restrikt.plugin.backend

import org.jetbrains.org.objectweb.asm.Opcodes


internal const val ASM_VERSION = Opcodes.ASM9

private const val PACKAGE_PRIVATE_MASK = 0.inv() ushr 3

internal fun Int.setPackagePrivate() = this and PACKAGE_PRIVATE_MASK
