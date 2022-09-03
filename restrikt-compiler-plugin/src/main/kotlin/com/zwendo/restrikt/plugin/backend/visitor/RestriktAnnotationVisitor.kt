package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal class RestriktAnnotationVisitor(factory: () -> AnnotationVisitor) : AnnotationVisitor(ASM_VERSION) {

    private val original: AnnotationVisitor by lazy { factory() }

    override fun visit(name: String?, value: Any?) = context.addAction { original.visit(name, value) }

    override fun visitEnum(name: String?, descriptor: String?, value: String?) = context.addAction {
        original.visitEnum(name, descriptor, value)
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor

        preVisitSymbolDeclarationAnnotation(descriptor, context::currentClass)
        context.addAction { visitor = original.visitAnnotation(name, descriptor) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitArray(name: String?): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitArray(name) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitEnd() = context.addAction { original.visitEnd() }

}
