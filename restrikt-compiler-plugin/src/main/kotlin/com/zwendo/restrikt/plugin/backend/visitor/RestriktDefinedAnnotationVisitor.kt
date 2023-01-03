package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

/**
 * Visitor for annotations that are defined in the plugin.
 */
internal class RestriktDefinedAnnotationVisitor(
    private val annotationConfig: PluginConfiguration.AnnotationConfiguration,
    private val reasonName: String,
    factory: () -> AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private var messageVisited: Boolean = false

    private val original: AnnotationVisitor = if (!annotationConfig.keepAnnotation || !annotationConfig.enabled) {
        object : AnnotationVisitor(ASM_VERSION) {} // no-op
    } else {
        factory()
    }

    override fun visit(name: String?, value: Any?) {
        messageVisited = true
        original.visit(name, value)
    }

    override fun visitEnd() {
        if (!messageVisited && annotationConfig.defaultReason != null) {
            original.visit(reasonName, annotationConfig.defaultReason) // if no message, use the default one
        }
        original.visitEnd()
    }

}
