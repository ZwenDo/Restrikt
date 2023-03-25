package com.zwendo.restrikt.plugin.backend.symbol

import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.plugin.backend.logger
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import java.io.Closeable
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

internal class ClassData(
    private val outer: ClassData?,
    private val builder: ClassBuilder,
    baseAccess: Int,
    factory: (Int) -> ClassVisitor,
) : AbstractSymbolData<ClassVisitor>(outer, baseAccess, factory) {

    override var descriptor: DeclarationDescriptor? = null
        private set

    private var topLevel = false

    val isHiddenFromKotlin: Boolean
        get() = descriptor.hasHideFromKotlinAnnotation

    val access: Int
        get() = computeAccess()

    fun setDescriptor(descriptor: DeclarationDescriptor?) {
        if (this.descriptor != null || descriptor == null) return
        if (descriptor is PackageFragmentDescriptor) {
            topLevel = true
        } else { // if outer is present, we can set its descriptor
            outer?.setDescriptor(descriptor.containingDeclaration)
        }
        this.descriptor = descriptor
    }

    override fun hideFromKotlin() {
        if (!isWritten) {
            super.hideFromKotlin()
        }
    }

    override fun writeToClassFile(forceWriting: Boolean) {
        if (descriptor == null && !forceWriting) return // delaying writing until descriptor is set
        if (outer?.isWritten != true) {
            outer?.writeToClassFile(false) // writing outer class first
        }
        super.writeToClassFile(forceWriting)
    }

    override fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean = descriptor?.check() ?: false

    override fun symbolWriting(symbol: ClassVisitor) {
        if (PluginConfiguration.toplevelPrivateConstructor && topLevel) {
            generatePrivateConstructor()
        }
        super.symbolWriting(symbol)
    }

    override fun ClassVisitor.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor =
        visitAnnotation(descriptor, visible)

    fun setInternal() {
        isInternal = true
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

    private companion object {

        @JvmField
        val HIDE_FROM_KOTLIN_FQNAME = FqName(HideFromKotlin::class.java.name)

        private val DeclarationDescriptor?.hasHideFromKotlinAnnotation: Boolean
            get() = this?.annotations?.hasAnnotation(HIDE_FROM_KOTLIN_FQNAME) ?: false

    }

}
