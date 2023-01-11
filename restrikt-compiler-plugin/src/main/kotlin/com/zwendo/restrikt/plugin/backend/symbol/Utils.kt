package com.zwendo.restrikt.plugin.backend.symbol

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

val DeclarationDescriptor?.isInternal: Boolean
    get() = this?.accept(InternalVisibilityVisitor, Unit) ?: false

private object InternalVisibilityVisitor : DeclarationDescriptorVisitorEmptyBodies<Boolean, Unit>() {

    override fun visitFunctionDescriptor(descriptor: FunctionDescriptor, data: Unit): Boolean =
        descriptor.visibility == DescriptorVisibilities.INTERNAL

    override fun visitClassDescriptor(descriptor: ClassDescriptor, data: Unit): Boolean =
        descriptor.visibility == DescriptorVisibilities.INTERNAL

    override fun visitPropertyDescriptor(descriptor: PropertyDescriptor, data: Unit): Boolean =
        descriptor.visibility == DescriptorVisibilities.INTERNAL

    override fun visitPropertyGetterDescriptor(descriptor: PropertyGetterDescriptor, data: Unit): Boolean =
        descriptor.visibility == DescriptorVisibilities.INTERNAL

    override fun visitPropertySetterDescriptor(descriptor: PropertySetterDescriptor, data: Unit): Boolean =
        descriptor.visibility == DescriptorVisibilities.INTERNAL

    override fun visitDeclarationDescriptor(descriptor: DeclarationDescriptor?, data: Unit?): Boolean = false

}

fun generateDeprecatedHidden(visitorFactory: (String, Boolean) -> AnnotationVisitor) {
    visitorFactory(DEPRECATED_DESC, true).apply {
        val reason = PluginConfiguration.hideFromKotlin.deprecatedReason
        visit(DEPRECATED_MESSAGE_NAME, reason)
        visitEnum(DEPRECATED_LEVEL_NAME, DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
        visitEnd()
    }
}

private val DEPRECATED_DESC = Deprecated::class.java.desc

private val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc

private val DEPRECATED_MESSAGE_NAME = Deprecated::message.name

private val DEPRECATED_LEVEL_NAME = Deprecated::level.name
