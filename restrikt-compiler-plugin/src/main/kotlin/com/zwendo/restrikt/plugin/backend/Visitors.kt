package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.RestrictedToJava
import com.zwendo.restrikt.plugin.frontend.Config
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

internal class RestriktClassVisitor(
    original: ClassVisitor,
) : ClassVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        cv::visitAnnotation
    )

}


internal class RestriktFieldVisitor(
    original: FieldVisitor,
) : FieldVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        fv::visitAnnotation
    )

}


internal class RestriktMethodVisitor(
    original: MethodVisitor,
) : MethodVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean) = visitAnnotation(
        descriptor,
        visible,
        mv::visitAnnotation
    )

}


private fun visitAnnotation(
    descriptor: String,
    visible: Boolean,
    factory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor {
    return if (descriptor == HIDE_FROM_KOTLIN_DESC) {
        val original = if (Config.keepAnnotations) {
            factory(descriptor, visible)
        } else null
        HideFromKotlinVisitor(original, factory)
    } else {
        factory(descriptor, visible)
    }
}


private class HideFromKotlinVisitor(
    private val original: AnnotationVisitor?,
    private val visitorFactory: (String, Boolean) -> AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION) {

    private var message = RestrictedToJava.DEFAULT_MESSAGE

    override fun visit(name: String?, value: Any?) {
        message = value as String
    }

    override fun visitEnd() {
        original?.visitEnd()
        hide(message, visitorFactory)
    }

}

private fun hide(message: String, visitorFactory: (String, Boolean) -> AnnotationVisitor) {
    visitorFactory(DEPRECATED_DESC, false).apply {
        visit("message", message)
        visitEnum("level", DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
        visitEnd()
    }
}

private const val ASM_VERSION = Opcodes.ASM9

private val HIDE_FROM_KOTLIN_DESC = RestrictedToJava::class.java.desc

private val DEPRECATED_DESC = Deprecated::class.java.desc

private val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc
