package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.generateDeprecatedHidden
import com.zwendo.restrikt.plugin.backend.hasHideFromKotlin
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.MethodVisitor


internal class RestriktMethodVisitor(
    private val descriptor: DeclarationDescriptor?,
    private val classDescriptor: DeclarationDescriptor?,
    original: MethodVisitor,
) : MethodVisitor(ASM_VERSION, original) {

    override fun visitCode() {
        generateMarkers(descriptor, classDescriptor, mv::visitAnnotation)
        if (descriptor.hasHideFromKotlin || classDescriptor.hasHideFromKotlin) {
            generateDeprecatedHidden(mv::visitAnnotation)
        }
        super.visitCode()
    }

}
