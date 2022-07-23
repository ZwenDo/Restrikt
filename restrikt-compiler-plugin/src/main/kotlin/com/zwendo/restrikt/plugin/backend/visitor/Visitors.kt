package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.Constants
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor

internal class RestriktClassVisitor(
    original: ClassVisitor,
) : ClassVisitor(Constants.ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        cv::visitAnnotation
    )

}

internal class RestriktFieldVisitor(
    original: FieldVisitor,
) : FieldVisitor(Constants.ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        fv::visitAnnotation
    )

}

internal class RestriktMethodVisitor(
    original: MethodVisitor,
) : MethodVisitor(Constants.ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        mv::visitAnnotation
    )

}

private fun visitAnnotation(
    descriptor: String,
    visible: Boolean,
    originalFactory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor {
    val originalVisitor = originalFactory(descriptor, visible)
    return when (descriptor) {
        Constants.HIDE_FROM_KOTLIN_DESC -> HideFromKotlinVisitor(originalVisitor, originalFactory)
        else -> originalVisitor
    }
}
