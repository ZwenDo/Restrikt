package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.backend.visitor.RestriktClassVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktFieldVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktMetadataVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktMethodVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktRecordComponentVisitor
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.inline.SourceMapper
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor


internal class RestriktClassBuilder(private val original: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original

    override fun getVisitor(): ClassVisitor = RestriktClassVisitor { super.getVisitor() }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        lateinit var original: MethodVisitor
        val currentClassName = RestriktContext.currentClassName

        RestriktContext.addAction {
            val currentClass = RestriktContext.getClass(currentClassName)
            val function = currentClass?.function(name, desc)
            var actualAccess = if (function?.isSynthetic(currentClass) == true) {
                access or Opcodes.ACC_SYNTHETIC
            } else {
                access
            }

            actualAccess = actualAccess.setPackagePrivate()
            original = super.newMethod(origin, actualAccess, name, desc, signature, exceptions)
        }

        return RestriktMethodVisitor("$name$desc") { original }
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        lateinit var original: FieldVisitor
        val currentClassName = RestriktContext.currentClassName

        RestriktContext.addAction {
            val currentClass = RestriktContext.getClass(currentClassName)
            val field = currentClass?.property(name)
            val actualAccess = if (field?.isSynthetic(currentClass) == true) {
                access or Opcodes.ACC_SYNTHETIC
            } else {
                access
            }

            original = super.newField(origin, actualAccess, name, desc, signature, value)
        }

        return RestriktFieldVisitor(name) { original }
    }

    override fun visitSource(name: String, debug: String?) =
        RestriktContext.addAction { super.visitSource(name, debug) }

    override fun visitSMAP(smap: SourceMapper, backwardsCompatibleSyntax: Boolean) = RestriktContext.addAction {
        super.visitSMAP(smap, backwardsCompatibleSyntax)
    }

    override fun visitOuterClass(owner: String, name: String?, desc: String?) = RestriktContext.addAction {
        super.visitOuterClass(owner, name, desc)
    }

    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        RestriktContext.addAction {
            val clazz = RestriktContext.getClass(name)
            val actualAccess = if (clazz?.isInternal == true || clazz?.isForceSynthetic == true) {
                access or Opcodes.ACC_SYNTHETIC
            } else {
                access
            }

            super.visitInnerClass(name, outerName, innerName, actualAccess)
        }
    }

    override fun newRecordComponent(name: String, desc: String, signature: String?): RecordComponentVisitor {
        lateinit var original: RecordComponentVisitor
        RestriktContext.addAction { original = super.newRecordComponent(name, desc, signature) }
        return RestriktRecordComponentVisitor { original }
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
        RestriktContext.visitNewClass(name)
        RestriktContext.addAction {
            val clazz = RestriktContext.getClass(name)
            val actualAccess = if (clazz?.isInternal == true || clazz?.isForceSynthetic == true) {
                access or Opcodes.ACC_SYNTHETIC
            } else {
                access
            }

            super.defineClass(origin, version, actualAccess, name, signature, superName, interfaces)
        }
    }

    // called when metadata is visited
    override fun newAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
        lateinit var original: AnnotationVisitor
        RestriktContext.addAction { original = super.newAnnotation(desc, visible) }
        return RestriktMetadataVisitor { original }
    }


    override fun done() {
        RestriktContext.addAction { super.done() }
        RestriktContext.done()
    }

}
