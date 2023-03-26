package com.zwendo.restrikt.plugin.backend.visitor

import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal class RestriktAnnotationVisitor(
    private val actionAccumulator: (() -> Unit) -> Unit,
    factory: () -> AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private val original: AnnotationVisitor by lazy { factory() }

    override fun visit(name: String?, value: Any?): Unit = actionAccumulator { original.visit(name, value) }

    override fun visitEnum(name: String?, descriptor: String?, value: String?) = actionAccumulator {
        original.visitEnum(name, descriptor, value)
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor =
        RestriktAnnotationVisitor(actionAccumulator) {
            original.visitAnnotation(name, descriptor)
        }

    override fun visitArray(name: String?): AnnotationVisitor = RestriktAnnotationVisitor(actionAccumulator) {
        original.visitArray(name)
    }

    override fun visitEnd() = actionAccumulator { original.visitEnd() }

}
