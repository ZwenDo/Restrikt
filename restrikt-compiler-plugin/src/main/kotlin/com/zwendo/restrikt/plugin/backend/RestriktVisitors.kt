package com.zwendo.restrikt.plugin.backend

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor


internal class RestriktMethodVisitor(
    private val descriptor: DeclarationDescriptor?,
    private val classDescriptor: DeclarationDescriptor?,
    original: MethodVisitor,
) : MethodVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        generateRuntimeAnnotation(this.descriptor, descriptor, visible, mv::visitAnnotation)

    override fun visitCode() {
        if (descriptor.hasHideFromKotlin || classDescriptor.hasHideFromKotlin) {
            generateDeprecatedHidden(mv::visitAnnotation)
        }
        super.visitCode()
    }

}

internal class RestriktFieldVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: FieldVisitor,
) : FieldVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        generateRuntimeAnnotation(this.descriptor, descriptor, visible, fv::visitAnnotation)

}

internal class RestriktClassVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: ClassVisitor,
) : ClassVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        if (descriptor == HIDE_FROM_KOTLIN_DESC) {
            generateDeprecatedHidden(cv::visitAnnotation)
        }
        return generateRuntimeAnnotation(this.descriptor, descriptor, visible, cv::visitAnnotation)
    }

}
