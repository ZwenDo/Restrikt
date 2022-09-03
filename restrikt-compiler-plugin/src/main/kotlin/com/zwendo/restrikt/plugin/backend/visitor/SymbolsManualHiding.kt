package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.wrapper.KotlinSymbol
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

/**
 * Called when an annotation is visited during the context action queue traversal.
 */
internal fun visitSymbolDeclarationAnnotation(
    annotationDescriptor: String,
    runtimeVisibility: Boolean,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor = when (annotationDescriptor) {
    HIDE_FROM_KOTLIN_DESC -> HideFromKotlinVisitor.new(annotationDescriptor, runtimeVisibility, visitorFactory)
    HIDE_FROM_JAVA_DESC -> HideFromJavaVisitor.new(annotationDescriptor, runtimeVisibility, visitorFactory)
    PACKAGE_PRIVATE_DESC -> PackagePrivateVisitor.new(annotationDescriptor, runtimeVisibility, visitorFactory)
    else -> visitorFactory(annotationDescriptor, runtimeVisibility)
}

/**
 * Called when an annotation is visited for the first time (not when context queue is run).
 */
internal fun preVisitSymbolDeclarationAnnotation(annotationDescriptor: String, symbolProvider: () -> KotlinSymbol) =
    when (annotationDescriptor) {
        HIDE_FROM_JAVA_DESC -> symbolProvider().forceSynthetic()
        PACKAGE_PRIVATE_DESC -> symbolProvider().setPackagePrivate()
        else -> Unit
    }

internal val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc

internal val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

internal val PACKAGE_PRIVATE_DESC = PackagePrivate::class.java.desc


private class HideFromKotlinVisitor private constructor(
    private val original: AnnotationVisitor,
    private val visitorFactory: (String, Boolean) -> AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private var message = PluginConfiguration.hideFromKotlin.defaultReason

    override fun visit(name: String?, value: Any?) {
        message = value as String
        if (!PluginConfiguration.hideFromKotlin.enabled) {
            original.visit(name, value) // if disabled, keep the message on the annotation
        }
    }

    override fun visitEnd() {
        original.visitEnd()
        if (!PluginConfiguration.hideFromKotlin.enabled) return
        visitorFactory(DEPRECATED_DESC, true).apply {
            message?.let { visit(DEPRECATED_MESSAGE_NAME, it) }
            visitEnum(DEPRECATED_LEVEL_NAME, DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
            visitEnd()
        }
    }

    companion object {

        fun new(
            descriptor: String,
            visible: Boolean,
            factory: (String, Boolean) -> AnnotationVisitor,
        ): HideFromKotlinVisitor {
            val original = if (PluginConfiguration.hideFromKotlin.keepAnnotation) {
                factory(descriptor, visible)
            } else {
                RestriktNOPAnnotationVisitor
            }
            return HideFromKotlinVisitor(original, factory)
        }

        private val DEPRECATED_DESC = Deprecated::class.java.desc

        private val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc

        private val DEPRECATED_MESSAGE_NAME = Deprecated::message.name

        private val DEPRECATED_LEVEL_NAME = Deprecated::level.name

    }

}

private class HideFromJavaVisitor private constructor(
    private val original: AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private var messageVisited: Boolean = false

    override fun visit(name: String?, value: Any?) {
        messageVisited = true
        original.visit(name, value)
    }

    override fun visitEnd() {
        if (!messageVisited && DEFAULT_REASON != null) {
            original.visit(HIDE_FROM_JAVA_REASON_NAME, DEFAULT_REASON) // if no message, use the default one
        }
        original.visitEnd()
    }

    companion object {

        fun new(
            descriptor: String,
            visible: Boolean,
            factory: (String, Boolean) -> AnnotationVisitor,
        ): HideFromJavaVisitor {
            val original = if (
                PluginConfiguration.hideFromJava.keepAnnotation
                || !PluginConfiguration.hideFromJava.enabled
            ) { // disabled or keep annotation
                factory(descriptor, visible)
            } else {
                RestriktNOPAnnotationVisitor
            }
            return HideFromJavaVisitor(original)
        }

        private val HIDE_FROM_JAVA_REASON_NAME = HideFromJava::reason.name

        private val DEFAULT_REASON = PluginConfiguration.hideFromJava.defaultReason

    }

}

class PackagePrivateVisitor private constructor(
    private val original: AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private var messageVisited: Boolean = false

    override fun visit(name: String?, value: Any?) {
        messageVisited = true
        original.visit(name, value)
    }

    override fun visitEnd() {
//        if (!messageVisited && PluginConfiguration.packagePrivate.defaultReason != null) {
//            original.visit(PACKAGE_PRIVATE_REASON_NAME, PluginConfiguration.packagePrivate.defaultReason)
//        }
        original.visitEnd()
    }


    companion object {

        fun new(
            descriptor: String,
            visible: Boolean,
            factory: (String, Boolean) -> AnnotationVisitor,
        ): PackagePrivateVisitor {
            val original = if (true
//                PluginConfiguration.hideFromJava.keepAnnotation
//                || !PluginConfiguration.hideFromJava.enabled
            ) { // disabled or keep annotation
                factory(descriptor, visible)
            } else {
                RestriktNOPAnnotationVisitor
            }
            return PackagePrivateVisitor(original)
        }

        private val PACKAGE_PRIVATE_REASON_NAME = PackagePrivate::reason.name

        //private val DEFAULT_REASON = PluginConfiguration.packagePrivate.defaultReason
    }
}

private object RestriktNOPAnnotationVisitor : AnnotationVisitor(ASM_VERSION) {

    override fun visit(name: String?, value: Any?) = Unit

    override fun visitEnum(name: String?, desc: String?, value: String?) = Unit

    override fun visitAnnotation(name: String?, desc: String?) = RestriktNOPAnnotationVisitor

    override fun visitArray(name: String?) = RestriktNOPAnnotationVisitor

    override fun visitEnd() = Unit

}
