package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.ClassData
import com.zwendo.restrikt.plugin.backend.symbol.generateDeprecatedHidden
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor

/**
 * Proxy designed only to apply [HideFromKotlin][com.zwendo.restrikt.annotation.HideFromKotlin] hiding to classes.
 */
internal class RestriktClassVisitorProxy(
    private val classData: ClassData,
    inner: ClassVisitor
) : ClassVisitor(ASM_VERSION, inner) {

    private var hasBeenHidden = false

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        if (classData.isHiddenFromKotlin && !hasBeenHidden) {
            hasBeenHidden = true
            generateDeprecatedHidden(this::visitAnnotation)
        }
        return super.visitAnnotation(descriptor, visible)
    }

}
