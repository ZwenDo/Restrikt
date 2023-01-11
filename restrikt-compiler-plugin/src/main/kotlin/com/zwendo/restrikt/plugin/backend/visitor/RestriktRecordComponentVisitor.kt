package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor
import org.jetbrains.org.objectweb.asm.TypePath

internal class RestriktRecordComponentVisitor(
    private val context: SymbolData<RecordComponentVisitor>,
) : RecordComponentVisitor(ASM_VERSION) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        annotationVisitor(context) { visitAnnotation(descriptor, visible) }

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

    private inline fun queue(crossinline action: RecordComponentVisitor.() -> Unit) = context.queueAction { action() }

}
