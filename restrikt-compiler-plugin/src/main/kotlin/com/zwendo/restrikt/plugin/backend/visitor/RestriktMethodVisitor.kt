package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.generateDeprecatedHidden
import com.zwendo.restrikt.plugin.backend.hasHideFromKotlin
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor


internal class RestriktMethodVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: MethodVisitor,
) : MethodVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitSymbolDeclarationAnnotation(descriptor, visible) { s, v ->
            super.visitAnnotation(s, v)
        }

    override fun visitCode() {
        if (descriptor.hasHideFromKotlin) {
            generateDeprecatedHidden(mv::visitAnnotation)
        }
        super.visitCode()
    }

}
