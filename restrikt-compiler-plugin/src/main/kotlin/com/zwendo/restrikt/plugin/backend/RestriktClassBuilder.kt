package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.backend.visitor.RestriktMethodVisitor
import com.zwendo.restrikt.plugin.backend.visitor.generateMarkers
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.isTopLevelInPackage
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes


internal class RestriktClassBuilder(
    private val original: ClassBuilder,
    private val classDescriptor: DeclarationDescriptor?,
) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        val actualAccess = origin.descriptor.computeModifiers(access, classDescriptor)
        val original = super.newMethod(origin, actualAccess, name, desc, signature, exceptions)
        return RestriktMethodVisitor(origin.descriptor, classDescriptor, original)
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        val descriptor = origin.descriptor
        val actualAccess = descriptor.computeModifiers(access, classDescriptor)
        return super.newField(origin, actualAccess, name, desc, signature, value).apply {
            generateMarkers(descriptor, classDescriptor, this::visitAnnotation)
            if (descriptor.hasHideFromKotlin) {
                generateDeprecatedHidden(this::visitAnnotation)
            }
        }
    }

    override fun defineClass(
        origin: PsiElement?,
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>,
    ) {
        val actualAccess = classDescriptor.computeModifiers(access, null)
        original.defineClass(origin, version, actualAccess, name, signature, superName, interfaces)
        if (PluginConfiguration.toplevelPrivateConstructor && classDescriptor?.isTopLevelInPackage() == true) {
            generatePrivateConstructor()
        }
    }

    private fun generatePrivateConstructor() {
        val origin = JvmDeclarationOrigin(JvmDeclarationOriginKind.OTHER, null, null, null)
        val name = "<init>"
        val desc = "()V"
        val visitor = original.newMethod(origin, Opcodes.ACC_PRIVATE, name, desc, null, null)
        visitor.apply {
            visitCode()
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", name, desc, false)

            visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError")
            visitInsn(Opcodes.DUP)
            visitLdcInsn("This class should not be instantiated")
            visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/AssertionError",
                "<init>",
                "(Ljava/lang/Object;)V",
                false
            )
            visitInsn(Opcodes.ATHROW)

            visitMaxs(3, 1)
            visitEnd()
        }
    }

}
