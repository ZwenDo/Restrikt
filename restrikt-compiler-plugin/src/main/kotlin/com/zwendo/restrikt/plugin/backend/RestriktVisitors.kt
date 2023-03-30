package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor


internal class RestriktMethodVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: MethodVisitor,
) : MethodVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        tryGenerateDeprecated(descriptor, mv::visitAnnotation)
        return generateRuntimeAnnotation(this.descriptor, descriptor, visible, mv::visitAnnotation)
    }

}

internal class RestriktFieldVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: FieldVisitor,
) : FieldVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        tryGenerateDeprecated(descriptor, fv::visitAnnotation)
        return generateRuntimeAnnotation(this.descriptor, descriptor, visible, fv::visitAnnotation)
    }

}

internal class RestriktClassVisitor(
    private val descriptor: DeclarationDescriptor?,
    original: ClassVisitor,
) : ClassVisitor(ASM_VERSION, original) {

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        tryGenerateDeprecated(descriptor, cv::visitAnnotation)
        return generateRuntimeAnnotation(this.descriptor, descriptor, visible, cv::visitAnnotation)
    }

}

private fun tryGenerateDeprecated(descriptor: String, factory: (String, Boolean) -> AnnotationVisitor) {
    if (!PluginConfiguration.hideFromKotlin.enabled || descriptor != HIDE_FROM_KOTLIN_DESC) return
    generateDeprecatedHidden(factory)
}
