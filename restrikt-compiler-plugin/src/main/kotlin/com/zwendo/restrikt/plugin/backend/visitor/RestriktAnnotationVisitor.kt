package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal class RestriktAnnotationVisitor(
    private val context: SymbolData<*>,
    factory: () -> AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private val original: AnnotationVisitor by lazy { factory() }

    override fun visit(name: String?, value: Any?) = queue { original.visit(name, value) }

    override fun visitEnum(name: String?, descriptor: String?, value: String?) = queue {
        original.visitEnum(name, descriptor, value)
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor {
        preVisitSymbolDeclarationAnnotation(descriptor, context)
        return annotationVisitor(context) { original.visitAnnotation(name, descriptor) }
    }

    override fun visitArray(name: String?): AnnotationVisitor = annotationVisitor(context) { original.visitArray(name) }

    override fun visitEnd() = queue { original.visitEnd() }

    private inline fun queue(crossinline action: () -> Unit) = context.queueAction { action() }

}
