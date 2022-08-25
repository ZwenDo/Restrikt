package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor
import org.jetbrains.org.objectweb.asm.TypePath

internal class RestriktRecordComponentVisitor(
    factory: () -> RecordComponentVisitor,
) : RecordComponentVisitor(ASM_VERSION) {

    private val original: RecordComponentVisitor by lazy { factory() }

    override fun getDelegate(): RecordComponentVisitor = this

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction {
            visitor = visitSymbolDeclarationAnnotation(descriptor, visible, original::visitAnnotation)
        }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = visitTypeAnnotation(typeRef, typePath, descriptor, visible) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitAttribute(attribute: Attribute?) = context.addAction { original.visitAttribute(attribute) }

    override fun visitEnd() = context.addAction { original.visitEnd() }

}
