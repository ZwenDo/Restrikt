package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

internal const val ASM_VERSION = Opcodes.ASM9

/**
 * Called when an annotation is visited during the context action queue traversal.
 */
internal fun visitSymbolDeclarationAnnotation(
    descriptor: String,
    visible: Boolean,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor = when (descriptor) {
    HIDE_FROM_KOTLIN_DESC -> RestriktDefinedAnnotationVisitor(
        descriptor,
        PluginConfiguration.hideFromKotlin,
        HideFromKotlin::reason.name,
        HideFromKotlin::retention.name,
        visitorFactory
    )
    HIDE_FROM_JAVA_DESC -> RestriktDefinedAnnotationVisitor(
        descriptor,
        PluginConfiguration.hideFromJava,
        HideFromJava::reason.name,
        HideFromJava::retention.name,
        visitorFactory
    )
    PACKAGE_PRIVATE_DESC -> RestriktDefinedAnnotationVisitor(
        descriptor,
        PluginConfiguration.packagePrivate,
        PackagePrivate::reason.name,
        PackagePrivate::retention.name,
        visitorFactory
    )
    else -> visitorFactory(descriptor, visible)
}

private val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc

private val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

private val PACKAGE_PRIVATE_DESC = PackagePrivate::class.java.desc
