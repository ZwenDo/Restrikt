package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.descriptors.IrBasedClassDescriptor
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes


internal class RestriktClassBuilder(
    private val original: ClassBuilder,
    private val classOrigin: JvmDeclarationOrigin,
) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original

    override fun getVisitor(): ClassVisitor = RestriktClassVisitor(classOrigin.descriptor, original.visitor)

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
        val original = super.newField(origin, actualAccess, name, desc, signature, value)
        if (descriptor.hasHideFromKotlin) {
            generateDeprecatedHidden(original::visitAnnotation)
        }
        return RestriktFieldVisitor(descriptor, original)
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
        if (PluginConfiguration.toplevelPrivateConstructor && classOrigin.shouldGeneratePrivateConstructor) {
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

    private val classDescriptor: DeclarationDescriptor?
        get() = classOrigin.descriptor

    private val JvmDeclarationOrigin.shouldGeneratePrivateConstructor: Boolean
        get() {
            if (originKind == JvmDeclarationOriginKind.PACKAGE_PART) return true
            val desc = descriptor
            if (desc !is IrBasedClassDescriptor) return false
            val origin = desc.owner.origin
            return origin == IrDeclarationOrigin.JVM_MULTIFILE_CLASS
                        || origin == IrDeclarationOrigin.FILE_CLASS
                        || origin == IrDeclarationOrigin.SYNTHETIC_FILE_CLASS
        }

}
