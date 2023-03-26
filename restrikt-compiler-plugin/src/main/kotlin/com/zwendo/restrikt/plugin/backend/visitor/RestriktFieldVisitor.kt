package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.generateDeprecatedHidden
import com.zwendo.restrikt.plugin.backend.hasHideFromKotlin
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor

internal class RestriktFieldVisitor(
    descriptor: DeclarationDescriptor?,
    original: FieldVisitor,
) : FieldVisitor(ASM_VERSION, original) {

    init {
        if (descriptor.hasHideFromKotlin) {
            generateDeprecatedHidden(original::visitAnnotation)
        }
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitSymbolDeclarationAnnotation(descriptor, visible,) { s, v ->
            super.visitAnnotation(s, v)
        }

}
