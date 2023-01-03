package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktFieldVisitor(
    private val context: SymbolData<FieldVisitor>
) : FieldVisitor(ASM_VERSION) {


    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        preVisitSymbolDeclarationAnnotation(descriptor, context)
        return annotationVisitor(context) {
            visitSymbolDeclarationAnnotation(descriptor) { visitAnnotation(descriptor, visible) }
        }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute?) = queue { visitAttribute(attribute) }

    override fun visitEnd() = queue { visitEnd() }

    private inline fun queue(crossinline action: FieldVisitor.() -> Unit) {
        context.queueAction { action() }
    }

}
