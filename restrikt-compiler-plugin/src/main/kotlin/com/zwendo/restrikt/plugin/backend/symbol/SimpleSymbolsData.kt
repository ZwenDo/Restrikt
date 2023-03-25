package com.zwendo.restrikt.plugin.backend.symbol

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor

internal class BasicMethodData(
    classData: ClassData,
    override val descriptor: DeclarationDescriptor?,
    baseAccess: Int,
    symbolFactory: (Int) -> MethodVisitor,
) : AbstractSymbolData<MethodVisitor>(classData, baseAccess, symbolFactory) {

    override fun MethodVisitor.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitAnnotation(descriptor, visible)

    override fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean = descriptor?.check() ?: false

}

internal class FieldData(
    classData: ClassData,
    override val descriptor: DeclarationDescriptor?,
    baseAccess: Int,
    symbolFactory: (Int) -> FieldVisitor,
) : AbstractSymbolData<FieldVisitor>(classData, baseAccess, symbolFactory) {

    override fun FieldVisitor.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitAnnotation(descriptor, visible)

    override fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean = descriptor?.check() ?: false

}


internal class RecordComponentData(
    classData: ClassData,
    symbolFactory: (Int) -> RecordComponentVisitor,
) : AbstractSymbolData<RecordComponentVisitor>(classData, 0, symbolFactory) {

    override val descriptor: DeclarationDescriptor?
        get() = null

    override fun RecordComponentVisitor.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitAnnotation(descriptor, visible)

    override fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean = false

}
