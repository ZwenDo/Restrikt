package com.zwendo.restrikt.plugin.backend

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor

internal class RestriktClassBuilder(private val original: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original

    override fun getVisitor(): ClassVisitor = RestriktClassVisitor(original.visitor)

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        val originalVisitor = original.newMethod(origin, access, name, desc, signature, exceptions)
        return RestriktMethodVisitor(originalVisitor)
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        val originalVisitor = original.newField(origin, access, name, desc, signature, value)
        return RestriktFieldVisitor(originalVisitor)
    }

}
