package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.backend.visitor.RestriktClassVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktFieldVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktMethodVisitor
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes


internal class RestriktClassBuilder(private val original: ClassBuilder) : DelegatingClassBuilder() {

    private var currentVisitor: RestriktClassVisitor? = null

    override fun getDelegate(): ClassBuilder = original

    override fun getVisitor(): ClassVisitor {
        val originalVisitor = super.getVisitor()
        val current = classStack.last()
        return if (current.written) {
            originalVisitor
        } else {
            RestriktClassVisitor(original.visitor, current::forceWrite, false).also {
                currentVisitor = it
            }
        }
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        updateClassContext(origin)
        val original = super.newMethod(
            origin,
            origin.descriptor.computeModifiers(access),
            name,
            desc,
            signature,
            exceptions
        )
        return RestriktMethodVisitor(origin.descriptor, original)
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        updateClassContext(origin)
        val original = super.newField(origin, origin.descriptor.computeModifiers(access), name, desc, signature, value)
        return RestriktFieldVisitor(origin.descriptor, original)
    }

    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        val self = classStack.findLast { it.name == name }
        val actualAccess = self?.descriptor.computeModifiers(access)
        super.visitInnerClass(name, outerName, innerName, actualAccess)
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
        classStack += ClassContext(original, name) {
            original.defineClass(origin, version, it(access), name, signature, superName, interfaces)
        }
    }

    override fun done(generateSmapCopyToAnnotation: Boolean) {
        val top = classStack.pop()
        if (!top.written) {
            top.forceWrite()
        }
        currentVisitor?.write()
        currentVisitor = null
        super.done(generateSmapCopyToAnnotation)
    }

    private fun updateClassContext(origin: JvmDeclarationOrigin) {
        classStack.peek()?.setDescriptor(origin.descriptor?.containingDeclaration)
    }

    private companion object {

        val classStack = ArrayList<ClassContext>()

    }

}

private class ClassContext(
    private val builder: ClassBuilder,
    val name: String,
    private var definitionLambda: ((Int) -> Int) -> Unit,
) {

    var descriptor: DeclarationDescriptor? = null
        private set

    var written: Boolean = false
        private set

    fun setDescriptor(descriptor: DeclarationDescriptor?) {
        if (written || descriptor == null) return
        this.descriptor = descriptor
        write(this.descriptor::computeModifiers)
    }

    fun forceWrite(block: (Int) -> Int = { it }) {
        require(!written) { "Class $name already written" }
        write(block)
    }

    private fun write(block: (Int) -> Int) {
        written = true
        if (PluginConfiguration.toplevelPrivateConstructor && descriptor is PackageFragmentDescriptor) {
            generatePrivateConstructor()
        }
        definitionLambda(block)
    }

    private fun generatePrivateConstructor() {
        val origin = JvmDeclarationOrigin(JvmDeclarationOriginKind.OTHER, null, null, null)
        val name = "<init>"
        val desc = "()V"
        val visitor = builder.newMethod(origin, Opcodes.ACC_PRIVATE, name, desc, null, null)
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

    override fun toString(): String = "$name $written"

}
