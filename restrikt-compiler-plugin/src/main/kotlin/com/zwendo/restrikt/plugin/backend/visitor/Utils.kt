package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

internal fun <T> annotationVisitor(
    context: SymbolData<T>,
    proxyFactory: (SymbolData<T>, () -> AnnotationVisitor) -> AnnotationVisitor = ::RestriktAnnotationVisitor,
    visitorFactory: T.() -> AnnotationVisitor,
): AnnotationVisitor {
    lateinit var visitor: AnnotationVisitor
    context.queueAction { visitor = visitorFactory() }
    return proxyFactory(context) { visitor }
}

/**
 * Called when an annotation is visited during the context action queue traversal.
 */
internal fun visitSymbolDeclarationAnnotation(
    descriptor: String,
    visitorFactory: () -> AnnotationVisitor,
): AnnotationVisitor = when (descriptor) {
    HIDE_FROM_KOTLIN_DESC -> RestriktDefinedAnnotationVisitor(
        PluginConfiguration.hideFromKotlin,
        HideFromKotlin::reason.name,
        visitorFactory
    ) //HideFromKotlinVisitor.new(annotationDescriptor, runtimeVisibility, visitorFactory)
    HIDE_FROM_JAVA_DESC -> RestriktDefinedAnnotationVisitor(
        PluginConfiguration.hideFromJava,
        HideFromJava::reason.name,
        visitorFactory
    )
    PACKAGE_PRIVATE_DESC -> RestriktDefinedAnnotationVisitor(
        PluginConfiguration.packagePrivate,
        PackagePrivate::reason.name,
        visitorFactory
    )
    else -> visitorFactory()
}

/**
 * Called when an annotation is visited for the first time (not when context queue is run).
 * Used to detect annotations that may influence modifiers.
 */
internal fun preVisitSymbolDeclarationAnnotation(annotationDescriptor: String, context: SymbolData<*>) =
    when (annotationDescriptor) {
        HIDE_FROM_JAVA_DESC -> context.hideFromJava()
        PACKAGE_PRIVATE_DESC -> context.setPackagePrivate()
        HIDE_FROM_KOTLIN_DESC -> context.hideFromKotlin()
        else -> Unit
    }


internal val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc

internal val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

internal val PACKAGE_PRIVATE_DESC = PackagePrivate::class.java.desc
