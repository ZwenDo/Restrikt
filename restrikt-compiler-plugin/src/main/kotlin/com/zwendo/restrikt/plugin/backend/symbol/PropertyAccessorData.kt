package com.zwendo.restrikt.plugin.backend.symbol

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor

/**
 * Property getter or setter data.
 */
internal class PropertyAccessorData(
    classData: ClassData,
    override val descriptor: PropertyAccessorDescriptor?,
    baseAccess: Int,
    symbolFactory: (Int) -> MethodVisitor,
) : AbstractSymbolData<MethodVisitor>(classData, baseAccess, symbolFactory) {

    private val property: PropertyDescriptor?
        get() = descriptor?.correspondingProperty

    override fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean =
        descriptor?.check() == true || property?.check() == true

    override fun MethodVisitor.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitAnnotation(descriptor, visible)

}
