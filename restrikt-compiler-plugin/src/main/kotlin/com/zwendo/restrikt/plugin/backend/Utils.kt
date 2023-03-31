package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes


internal const val ASM_VERSION = Opcodes.ASM9

internal val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc


internal fun generateDeprecatedHidden(visitorFactory: (String, Boolean) -> AnnotationVisitor) {
    visitorFactory(DEPRECATED_DESC, true).apply {
        val reason = PluginConfiguration.hideFromKotlin.deprecatedReason
        visit(DEPRECATED_MESSAGE_NAME, reason)
        visitEnum(DEPRECATED_LEVEL_NAME, DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
        visitEnd()
    }
}

private val DEPRECATED_DESC = Deprecated::class.java.desc

private val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc

private val DEPRECATED_MESSAGE_NAME = Deprecated::message.name

private val DEPRECATED_LEVEL_NAME = Deprecated::level.name
