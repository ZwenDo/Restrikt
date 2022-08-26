package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal fun visitSymbolDeclarationAnnotation(
    descriptor: String,
    visible: Boolean,
    factory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor = when (descriptor) {
    HIDE_FROM_KOTLIN_DESC -> HideFromKotlinVisitor.new(descriptor, visible, factory)
    HIDE_FROM_JAVA_DESC -> HideFromJavaVisitor.new(descriptor, visible, factory)
    else -> factory(descriptor, visible)
}

internal inline fun checkHideFromJava(descriptor: String, hideAction: () -> Unit) {
    if (PluginConfiguration.hideFromJava.enabled && descriptor == HIDE_FROM_JAVA_DESC) {
        hideAction()
    }
}


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
        visitorFactory(DEPRECATED_DESC, false).apply {
            message?.let { visit("message", it) }
            visitEnum("level", DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
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
        if (!messageVisited && PluginConfiguration.hideFromJava.defaultReason != null) {
            original.visit("reason", PluginConfiguration.hideFromJava.defaultReason)
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

    }

}


private val HIDE_FROM_KOTLIN_DESC = HideFromKotlin::class.java.desc

private val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

private object RestriktNOPAnnotationVisitor : AnnotationVisitor(ASM_VERSION) {

    override fun visit(name: String?, value: Any?) = Unit

    override fun visitEnum(name: String?, desc: String?, value: String?) = Unit

    override fun visitAnnotation(name: String?, desc: String?) = RestriktNOPAnnotationVisitor

    override fun visitArray(name: String?) = RestriktNOPAnnotationVisitor

    override fun visitEnd() = Unit

}
