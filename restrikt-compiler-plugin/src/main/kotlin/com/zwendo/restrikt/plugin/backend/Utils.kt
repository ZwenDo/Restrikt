package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

internal fun DeclarationDescriptor?.computeModifiers(initial: Int, classDescriptor: DeclarationDescriptor?): Int {
    if (this == null) return initial
    var access = initial

    if (this !is ClassDescriptor && mustBeSynthetic || classDescriptor.mustBeSynthetic) {
        access = access or Opcodes.ACC_SYNTHETIC
    }
    if (hasPackagePrivate) access = access and PACKAGE_PRIVATE_MASK

    return access
}

internal fun generateDeprecatedHidden(visitorFactory: (String, Boolean) -> AnnotationVisitor) {
    visitorFactory(DEPRECATED_DESC, true).apply {
        val reason = PluginConfiguration.hideFromKotlin.deprecatedReason
        visit(DEPRECATED_MESSAGE_NAME, reason)
        visitEnum(DEPRECATED_LEVEL_NAME, DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
        visitEnd()
    }
}

internal val DeclarationDescriptor?.hasHideFromKotlin: Boolean
    get() = PluginConfiguration.hideFromKotlin.enabled && hasAnnotation(HIDE_FROM_KOTLIN_FQNAME)

internal val HIDE_FROM_KOTLIN_FQNAME = FqName(HideFromKotlin::class.java.canonicalName)

internal val HIDE_FROM_JAVA_FQNAME = FqName(HideFromJava::class.java.canonicalName)

internal val PACKAGE_PRIVATE_FQNAME = FqName(PackagePrivate::class.java.canonicalName)

private val DeclarationDescriptor?.hasHideFromJava: Boolean
    get() = PluginConfiguration.hideFromJava.enabled && hasAnnotation(HIDE_FROM_JAVA_FQNAME)

private val DeclarationDescriptor?.hasPackagePrivate: Boolean
    get() = PluginConfiguration.packagePrivate.enabled && hasAnnotation(PACKAGE_PRIVATE_FQNAME)

private val DeclarationDescriptor?.mustBeSynthetic: Boolean
    get() = this != null && (isInternal || hasHideFromJava || containingDeclaration.mustBeSynthetic)

private val DeclarationDescriptor?.isInternal: Boolean
    get() = PluginConfiguration.automaticInternalHiding
                && this is DeclarationDescriptorWithVisibility
                && visibility == DescriptorVisibilities.INTERNAL

private fun DeclarationDescriptor?.hasAnnotation(fqName: FqName): Boolean {
    if (this == null) return false
    return annotations.hasAnnotation(fqName)
                || (this is PropertyAccessorDescriptor && correspondingProperty.hasAnnotation(fqName))
}

private val DEPRECATED_DESC = Deprecated::class.java.desc

private val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc

private val DEPRECATED_MESSAGE_NAME = Deprecated::message.name

private val DEPRECATED_LEVEL_NAME = Deprecated::level.name

private const val PACKAGE_PRIVATE_MASK =
    0.inv() xor Opcodes.ACC_PUBLIC xor Opcodes.ACC_PRIVATE xor Opcodes.ACC_PROTECTED
